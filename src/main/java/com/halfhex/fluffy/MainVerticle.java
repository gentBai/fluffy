package com.halfhex.fluffy;

import com.halfhex.fluffy.config.ConfigHolder;
import com.halfhex.fluffy.config.ConfigVerticle;
import com.halfhex.fluffy.entity.GatewayRoute;
import com.halfhex.fluffy.entity.ServiceInstance;
import com.halfhex.fluffy.gateway.BackendClient;
import com.halfhex.fluffy.gateway.LoadBalancer;
import com.halfhex.fluffy.gateway.RouteHandler;
import com.halfhex.fluffy.entity.AccessLog;
import com.halfhex.fluffy.repository.AccessLogRepository;
import com.halfhex.fluffy.repository.RateLimitRuleRepository;
import com.halfhex.fluffy.security.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.web.Router;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class MainVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

  private Pool mysqlPool;
  private Redis redisClient;
  private ConfigHolder configHolder;
  private RouteHandler routeHandler;
  private LoadBalancer loadBalancer;
  private BackendClient backendClient;
  private ApiKeyHandler apiKeyHandler;
  private JwtHandler jwtHandler;
  private RateLimiter rateLimiter;
  private SecurityChecker securityChecker;
  private boolean ownResources = false;

  @Override
  public void start(Promise<Void> startPromise) {
    initialize()
      .compose(v -> startGatewayServer())
      .compose(v -> startAdminServer())
      .onComplete(startPromise);
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    if (routeHandler != null) {
      routeHandler.stopPeriodicRefresh();
    }
    if (jwtHandler != null) {
      jwtHandler.close();
    }
    Future<Void> closeRedis = (ownResources && redisClient != null) ? redisClient.close() : Future.succeededFuture();
    Future<Void> closePool = (ownResources && mysqlPool != null) ? mysqlPool.close() : Future.succeededFuture();
    Future.all(closeRedis, closePool).onComplete(ar -> {
      if (ar.succeeded()) stopPromise.complete();
      else stopPromise.fail(ar.cause());
    });
  }

  private Future<Void> initialize() {
    LocalMap<String, Object> shared = vertx.sharedData().getLocalMap("fluffy.config");
    ConfigHolder holder = (ConfigHolder) shared.get(ConfigVerticle.CONFIG_HOLDER_KEY);
    Pool pool = (Pool) shared.get(ConfigVerticle.MYSQL_CLIENT_KEY);
    Redis redis = (Redis) shared.get(ConfigVerticle.REDIS_CLIENT_KEY);

    if (holder != null && pool != null && redis != null) {
      this.configHolder = holder;
      this.mysqlPool = pool;
      this.redisClient = redis;
      return initHandlers();
    }

    ownResources = true;
    return vertx.fileSystem().readFile("application.conf")
      .map(content -> content.toString(StandardCharsets.UTF_8))
      .map(JsonObject::new)
      .compose(config -> {
        ConfigHolder.init(config);
        this.configHolder = ConfigHolder.getInstance();

        String redisConn = "redis://" + configHolder.getRedisHost() + ":" + configHolder.getRedisPort();
        RedisOptions redisOpts = new RedisOptions()
          .setConnectionString(redisConn)
          .setMaxPoolSize(configHolder.getRedisMaxPoolSize());
        this.redisClient = Redis.createClient(vertx, redisOpts);

        MySQLConnectOptions mysqlOpts = new MySQLConnectOptions()
          .setHost(configHolder.getDbHost())
          .setPort(configHolder.getDbPort())
          .setDatabase(configHolder.getDbDatabase())
          .setUser(configHolder.getDbUsername())
          .setPassword(configHolder.getDbPassword())
          .setCharset("utf8mb4");
        PoolOptions poolOpts = new PoolOptions().setMaxSize(configHolder.getDbMaxPoolSize());
        this.mysqlPool = Pool.pool(vertx, mysqlOpts, poolOpts);

        return initHandlers();
      });
  }

  private Future<Void> initHandlers() {
    this.routeHandler = new RouteHandler(mysqlPool, vertx);
    this.loadBalancer = new LoadBalancer(mysqlPool);
    this.backendClient = new BackendClient(vertx, configHolder);
    this.apiKeyHandler = new ApiKeyHandler(mysqlPool, redisClient);
    this.jwtHandler = new JwtHandler(vertx, mysqlPool);
    this.rateLimiter = new RateLimiter(redisClient);
    this.securityChecker = new SecurityChecker(mysqlPool, redisClient);

    this.routeHandler.startPeriodicRefresh();
    return jwtHandler.initialize();
  }

  private Future<Void> startGatewayServer() {
    HttpServer server = vertx.createHttpServer();
    server.requestHandler(this::handleGatewayRequest);
    return server.listen(configHolder.getAppPort())
      .map(s -> {
        logger.info("Gateway server started on port {}", s.actualPort());
        return null;
      });
  }

  private Future<Void> startAdminServer() {
    Router adminRouter = new AdminApiRouter(mysqlPool, jwtHandler).createRouter(vertx);
    HttpServer server = vertx.createHttpServer();
    server.requestHandler(adminRouter);
    return server.listen(configHolder.getAppAdminPort())
      .map(s -> {
        logger.info("Admin server started on port {}", s.actualPort());
        return null;
      });
  }

  private void handleGatewayRequest(HttpServerRequest req) {
    long startTime = System.currentTimeMillis();
    String clientIp = req.remoteAddress() != null ? req.remoteAddress().host() : "unknown";
    final GatewayRoute[] matchedRoute = new GatewayRoute[1];
    final ServiceInstance[] selectedInstance = new ServiceInstance[1];

    securityChecker.checkAccess(clientIp, null, null)
      .compose(result -> {
        if (result == SecurityCheckResult.BLOCKED) {
          return Future.failedFuture(new GatewayException(403, "Access denied"));
        }
        return routeHandler.matchRoute(req.path(), req.method().name());
      })
      .compose(route -> {
        if (route == null) {
          return Future.failedFuture(new GatewayException(404, "Route not found"));
        }
        matchedRoute[0] = route;
        return processRoute(req, route, clientIp, selectedInstance);
      })
      .onComplete(ar -> {
        int status = 200;
        Throwable error = null;
        if (ar.failed()) {
          error = ar.cause();
          status = 500;
          if (error instanceof GatewayException) {
            status = ((GatewayException) error).getStatusCode();
          }
          if (!req.response().ended()) {
            req.response().setStatusCode(status).end(error.getMessage());
          }
        }
        logAccess(req, matchedRoute[0], selectedInstance[0], status, startTime, error);
      });
  }

  private Future<Void> processRoute(HttpServerRequest req, GatewayRoute route, String clientIp, ServiceInstance[] instanceHolder) {
    if (route.getServiceId() == null) {
      return Future.failedFuture(new GatewayException(503, "No upstream service configured"));
    }

    Future<Void> authCheck = Future.succeededFuture();
    if (Boolean.TRUE.equals(route.getAuthRequired())) {
      authCheck = checkAuth(req);
    }

    return authCheck
      .compose(v -> checkRateLimit(route, clientIp))
      .compose(v -> loadBalancer.selectInstance(route.getServiceId()))
      .compose(instance -> {
        instanceHolder[0] = instance;
        return backendClient.forward(req, instance, req.uri());
      })
      .compose(response -> {
        req.response().setStatusCode(response.statusCode());
        response.headers().forEach(header -> {
          if (!BackendClient.HOP_BY_HOP_HEADERS.contains(header.getKey().toLowerCase())) {
            req.response().putHeader(header.getKey(), header.getValue());
          }
        });
        Buffer body = response.body();
        if (body != null) {
          req.response().end(body);
        } else {
          req.response().end();
        }
        return Future.succeededFuture();
      });
  }

  private Future<Void> checkAuth(HttpServerRequest req) {
    String authHeader = req.getHeader("Authorization");
    String token = ApiKeyHandler.extractApiKeyFromHeader(authHeader);

    return apiKeyHandler.validateApiKey(token)
      .map(v -> (Void) null)
      .recover(err -> jwtHandler.validateToken(token).map(v -> null))
      .recover(err -> Future.failedFuture(new GatewayException(401, "Unauthorized")));
  }

  private Future<Void> checkRateLimit(GatewayRoute route, String clientIp) {
    if (!Boolean.TRUE.equals(route.getRateLimitEnabled())) {
      return Future.succeededFuture();
    }

    RateLimitRuleRepository repo = new RateLimitRuleRepository(mysqlPool);
    Promise<com.halfhex.fluffy.entity.RateLimitRule> promise = Promise.promise();
    repo.findByRouteId(route.getId(), promise);

    return promise.future()
      .recover(err -> {
        if (err.getMessage() != null && err.getMessage().toLowerCase().contains("not found")) {
          return Future.succeededFuture(null);
        }
        return Future.failedFuture(err);
      })
      .compose(entityRule -> {
        if (entityRule == null) {
          return Future.succeededFuture();
        }
        String limitTypeStr = entityRule.getLimitType() != null ? entityRule.getLimitType() : "IP";
        RateLimitType type;
        try {
          type = RateLimitType.valueOf(limitTypeStr);
        } catch (IllegalArgumentException e) {
          type = RateLimitType.IP;
        }
        String identifier = clientIp;
        if (type == RateLimitType.USERNAME) {
          identifier = "anonymous";
        } else if (type == RateLimitType.GLOBAL) {
          identifier = "global";
        }

        RateLimitRule rule = new RateLimitRule(
          entityRule.getRequestsPerMinute() != null ? entityRule.getRequestsPerMinute() : 0,
          entityRule.getRequestsPerHour() != null ? entityRule.getRequestsPerHour() : 0,
          entityRule.getRequestsPerDay() != null ? entityRule.getRequestsPerDay() : 0,
          entityRule.getBurstSize() != null ? entityRule.getBurstSize() : 10
        );

        return rateLimiter.checkRateLimit(type, identifier, rule)
          .compose(result -> {
            if (!result.isAllowed()) {
              return Future.failedFuture(new GatewayException(429, "Rate limit exceeded"));
            }
            return Future.succeededFuture();
          });
      });
  }

  private void logAccess(HttpServerRequest req, GatewayRoute route, ServiceInstance instance,
                         int statusCode, long startTime, Throwable error) {
    try {
      long duration = System.currentTimeMillis() - startTime;
      AccessLog log = new AccessLog();
      log.setTraceId(java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16));
      log.setRequestMethod(req.method().name());
      log.setRequestPath(req.path());
      log.setRequestQuery(req.query());
      log.setResponseStatus(statusCode);
      log.setResponseTimeMs((int) duration);
      log.setClientIp(req.remoteAddress() != null ? req.remoteAddress().host() : "unknown");
      log.setRouteId(route != null ? route.getId() : null);
      log.setServiceId(route != null ? route.getServiceId() : null);
      log.setInstanceId(instance != null ? instance.getId() : null);
      if (error != null) {
        log.setErrorMessage(error.getMessage());
      }

      AccessLogRepository repo = new AccessLogRepository(mysqlPool);
      io.vertx.core.Promise<Void> p = io.vertx.core.Promise.promise();
      repo.save(log, p);
      p.future().onFailure(err -> logger.warn("Failed to write access log: {}", err.getMessage()));
    } catch (Exception e) {
      logger.warn("Access log creation failed: {}", e.getMessage());
    }
  }
}
