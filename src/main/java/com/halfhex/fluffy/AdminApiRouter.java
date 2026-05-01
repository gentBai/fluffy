package com.halfhex.fluffy;

import com.halfhex.fluffy.entity.*;
import com.halfhex.fluffy.repository.*;
import com.halfhex.fluffy.security.JwtHandler;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AdminApiRouter {

  private final Pool mysqlPool;
  private final JwtHandler jwtHandler;

  public AdminApiRouter(Pool mysqlPool, JwtHandler jwtHandler) {
    this.mysqlPool = mysqlPool;
    this.jwtHandler = jwtHandler;
  }

  public Router createRouter(io.vertx.core.Vertx vertx) {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.route().handler(this::handleCors);

    router.post("/api/admin/auth/login").handler(this::handleLogin);
    router.route("/api/admin/*").handler(this::requireAuth);

    registerRouteRoutes(router);
    registerServiceRoutes(router);
    registerInstanceRoutes(router);
    registerApiKeyRoutes(router);
    registerJwtConfigRoutes(router);
    registerRateLimitRoutes(router);
    registerBlacklistRoutes(router);
    registerWhitelistRoutes(router);
    registerLogRoutes(router);

    router.errorHandler(404, ctx -> ctx.response().setStatusCode(404).end(jsonMessage("Not found")));
    router.errorHandler(500, ctx -> ctx.response().setStatusCode(500).end(jsonMessage("Internal Server Error")));

    return router;
  }

  private void handleCors(RoutingContext ctx) {
    ctx.response()
      .putHeader("Access-Control-Allow-Origin", "*")
      .putHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS")
      .putHeader("Access-Control-Allow-Headers", "Content-Type,Authorization");
    if (ctx.request().method() == HttpMethod.OPTIONS) {
      ctx.response().setStatusCode(204).end();
    } else {
      ctx.next();
    }
  }

  private void requireAuth(RoutingContext ctx) {
    String auth = ctx.request().getHeader("Authorization");
    if (auth == null || !auth.startsWith("Bearer ")) {
      ctx.response().setStatusCode(401).end(jsonMessage("Unauthorized"));
      return;
    }
    jwtHandler.validateToken(auth.substring(7)).onComplete(ar -> {
      if (ar.succeeded()) {
        ctx.put("userId", ar.result().getUserId());
        ctx.next();
      } else {
        ctx.response().setStatusCode(401).end(jsonMessage("Invalid token"));
      }
    });
  }

  private void handleLogin(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    String username = body != null ? body.getString("username") : null;
    String password = body != null ? body.getString("password") : null;
    if (username == null || username.isBlank() || password == null || password.isBlank()) {
      ctx.response().setStatusCode(400).end(jsonMessage("Username and password required"));
      return;
    }
    jwtHandler.generateToken(1L, Collections.emptyMap())
      .onSuccess(token -> ctx.json(new JsonObject().put("token", token)))
      .onFailure(err -> ctx.fail(500));
  }

  private void registerRouteRoutes(Router router) {
    GatewayRouteRepository repo = new GatewayRouteRepository(mysqlPool);
    router.get("/api/admin/routes").handler(ctx -> {
      Promise<List<GatewayRoute>> p = Promise.promise();
      repo.findAll(p);
      p.future()
        .map(list -> new JsonArray(list.stream().map(this::routeToJson).collect(Collectors.toList())))
        .onSuccess(ctx::json)
        .onFailure(err -> ctx.fail(500));
    });
    router.get("/api/admin/routes/:id").handler(ctx -> {
      Promise<GatewayRoute> p = Promise.promise();
      repo.findById(parseLong(ctx, "id"), p);
      p.future()
        .onSuccess(v -> ctx.json(routeToJson(v)))
        .onFailure(err -> notFoundOrFail(ctx, err));
    });
    router.post("/api/admin/routes").handler(ctx -> {
      JsonObject b = ctx.body().asJsonObject();
      GatewayRoute r = buildRoute(b);
      Promise<GatewayRoute> p = Promise.promise();
      repo.save(r, p);
      p.future()
        .onSuccess(v -> ctx.response().setStatusCode(201).end(routeToJson(v).encode()))
        .onFailure(err -> ctx.fail(500));
    });
    router.put("/api/admin/routes/:id").handler(ctx -> {
      JsonObject b = ctx.body().asJsonObject();
      GatewayRoute r = buildRoute(b);
      r.setId(parseLong(ctx, "id"));
      Promise<GatewayRoute> p = Promise.promise();
      repo.update(r, p);
      p.future()
        .onSuccess(v -> ctx.json(routeToJson(v)))
        .onFailure(err -> notFoundOrFail(ctx, err));
    });
    router.delete("/api/admin/routes/:id").handler(ctx -> {
      Promise<Void> p = Promise.promise();
      repo.softDelete(parseLong(ctx, "id"), p);
      p.future()
        .onSuccess(v -> ctx.response().setStatusCode(204).end())
        .onFailure(err -> notFoundOrFail(ctx, err));
    });
  }

  private void registerServiceRoutes(Router router) {
    GatewayServiceRepository repo = new GatewayServiceRepository(mysqlPool);
    router.get("/api/admin/services").handler(ctx -> {
      Promise<List<GatewayService>> p = Promise.promise();
      repo.findAll(p);
      p.future()
        .map(list -> new JsonArray(list.stream().map(this::serviceToJson).collect(Collectors.toList())))
        .onSuccess(ctx::json)
        .onFailure(err -> ctx.fail(500));
    });
    router.get("/api/admin/services/:id").handler(ctx -> {
      Promise<GatewayService> p = Promise.promise();
      repo.findById(parseLong(ctx, "id"), p);
      p.future()
        .onSuccess(v -> ctx.json(serviceToJson(v)))
        .onFailure(err -> notFoundOrFail(ctx, err));
    });
    router.post("/api/admin/services").handler(ctx -> {
      JsonObject b = ctx.body().asJsonObject();
      GatewayService s = buildService(b);
      Promise<GatewayService> p = Promise.promise();
      repo.save(s, p);
      p.future()
        .onSuccess(v -> ctx.response().setStatusCode(201).end(serviceToJson(v).encode()))
        .onFailure(err -> ctx.fail(500));
    });
    router.put("/api/admin/services/:id").handler(ctx -> {
      JsonObject b = ctx.body().asJsonObject();
      GatewayService s = buildService(b);
      s.setId(parseLong(ctx, "id"));
      Promise<GatewayService> p = Promise.promise();
      repo.update(s, p);
      p.future()
        .onSuccess(v -> ctx.json(serviceToJson(v)))
        .onFailure(err -> notFoundOrFail(ctx, err));
    });
    router.delete("/api/admin/services/:id").handler(ctx -> {
      Promise<Void> p = Promise.promise();
      repo.softDelete(parseLong(ctx, "id"), p);
      p.future()
        .onSuccess(v -> ctx.response().setStatusCode(204).end())
        .onFailure(err -> notFoundOrFail(ctx, err));
    });
  }

  private void registerInstanceRoutes(Router router) {
    ServiceInstanceRepository repo = new ServiceInstanceRepository(mysqlPool);
    router.get("/api/admin/services/:serviceId/instances").handler(ctx -> {
      Promise<List<ServiceInstance>> p = Promise.promise();
      repo.findByServiceId(parseLong(ctx, "serviceId"), p);
      p.future()
        .map(list -> new JsonArray(list.stream().map(this::instanceToJson).collect(Collectors.toList())))
        .onSuccess(ctx::json)
        .onFailure(err -> ctx.fail(500));
    });
    router.post("/api/admin/services/:serviceId/instances").handler(ctx -> {
      JsonObject b = ctx.body().asJsonObject();
      ServiceInstance i = buildInstance(b);
      i.setServiceId(parseLong(ctx, "serviceId"));
      Promise<ServiceInstance> p = Promise.promise();
      repo.save(i, p);
      p.future()
        .onSuccess(v -> ctx.response().setStatusCode(201).end(instanceToJson(v).encode()))
        .onFailure(err -> ctx.fail(500));
    });
    router.put("/api/admin/services/:serviceId/instances/:id").handler(ctx -> {
      JsonObject b = ctx.body().asJsonObject();
      ServiceInstance i = buildInstance(b);
      i.setId(parseLong(ctx, "id"));
      i.setServiceId(parseLong(ctx, "serviceId"));
      Promise<ServiceInstance> p = Promise.promise();
      repo.update(i, p);
      p.future()
        .onSuccess(v -> ctx.json(instanceToJson(v)))
        .onFailure(err -> notFoundOrFail(ctx, err));
    });
    router.delete("/api/admin/services/:serviceId/instances/:id").handler(ctx -> {
      Promise<Void> p = Promise.promise();
      repo.softDelete(parseLong(ctx, "id"), p);
      p.future()
        .onSuccess(v -> ctx.response().setStatusCode(204).end())
        .onFailure(err -> notFoundOrFail(ctx, err));
    });
  }

  private void registerApiKeyRoutes(Router router) {
    ApiKeyRepository repo = new ApiKeyRepository(mysqlPool);
    router.get("/api/admin/api-keys").handler(ctx -> {
      Promise<List<ApiKey>> p = Promise.promise();
      repo.findAll(p);
      p.future()
        .map(list -> new JsonArray(list.stream().map(this::apiKeyToJson).collect(Collectors.toList())))
        .onSuccess(ctx::json)
        .onFailure(err -> ctx.fail(500));
    });
    router.post("/api/admin/api-keys").handler(ctx -> {
      JsonObject b = ctx.body().asJsonObject();
      ApiKey k = new ApiKey();
      k.setName(b.getString("name"));
      k.setRateLimitPerMinute(b.getInteger("rateLimitPerMinute", 1000));
      String expires = b.getString("expiresAt");
      k.setExpiresAt(expires != null ? LocalDateTime.parse(expires) : null);
      k.setKeyValue(java.util.UUID.randomUUID().toString().replace("-", ""));
      Promise<ApiKey> p = Promise.promise();
      repo.save(k, p);
      p.future()
        .onSuccess(v -> ctx.response().setStatusCode(201).end(apiKeyToJson(v).encode()))
        .onFailure(err -> ctx.fail(500));
    });
    router.put("/api/admin/api-keys/:id").handler(ctx -> {
      JsonObject b = ctx.body().asJsonObject();
      ApiKey k = new ApiKey();
      k.setId(parseLong(ctx, "id"));
      k.setName(b.getString("name"));
      k.setRateLimitPerMinute(b.getInteger("rateLimitPerMinute"));
      String expires = b.getString("expiresAt");
      k.setExpiresAt(expires != null ? LocalDateTime.parse(expires) : null);
      Promise<ApiKey> p = Promise.promise();
      repo.update(k, p);
      p.future()
        .onSuccess(v -> ctx.json(apiKeyToJson(v)))
        .onFailure(err -> notFoundOrFail(ctx, err));
    });
    router.delete("/api/admin/api-keys/:id").handler(ctx -> {
      Promise<Void> p = Promise.promise();
      repo.softDelete(parseLong(ctx, "id"), p);
      p.future()
        .onSuccess(v -> ctx.response().setStatusCode(204).end())
        .onFailure(err -> notFoundOrFail(ctx, err));
    });
  }

  private void registerJwtConfigRoutes(Router router) {
    router.get("/api/admin/jwt-config").handler(ctx ->
      mysqlPool.preparedQuery("SELECT id, issuer, algorithm, access_token_ttl_sec, refresh_token_ttl_sec FROM jwt_config WHERE deleted = 0 LIMIT 1")
        .execute()
        .map(rowSet -> {
          if (!rowSet.iterator().hasNext()) return null;
          Row row = rowSet.iterator().next();
          return new JsonObject()
            .put("id", row.getLong("id"))
            .put("issuer", row.getString("issuer"))
            .put("algorithm", row.getString("algorithm"))
            .put("accessTokenTtlSec", row.getInteger("access_token_ttl_sec"))
            .put("refreshTokenTtlSec", row.getInteger("refresh_token_ttl_sec"));
        })
        .onSuccess(v -> {
          if (v == null) ctx.response().setStatusCode(404).end(jsonMessage("JWT config not found"));
          else ctx.json(v);
        })
        .onFailure(err -> ctx.fail(500))
    );
    router.put("/api/admin/jwt-config").handler(ctx -> {
      JsonObject b = ctx.body().asJsonObject();
      mysqlPool.preparedQuery(
        "UPDATE jwt_config SET issuer = ?, algorithm = ?, access_token_ttl_sec = ?, refresh_token_ttl_sec = ?, updated_at = ? WHERE deleted = 0")
        .execute(Tuple.of(
          b.getString("issuer"),
          b.getString("algorithm"),
          b.getInteger("accessTokenTtlSec"),
          b.getInteger("refreshTokenTtlSec"),
          LocalDateTime.now()
        ))
        .mapEmpty()
        .onSuccess(v -> ctx.json(b))
        .onFailure(err -> ctx.fail(500));
    });
  }

  private void registerRateLimitRoutes(Router router) {
    RateLimitRuleRepository repo = new RateLimitRuleRepository(mysqlPool);
    router.get("/api/admin/rate-limit-rules").handler(ctx -> {
      Promise<List<RateLimitRule>> p = Promise.promise();
      repo.findAll(p);
      p.future()
        .map(list -> new JsonArray(list.stream().map(this::rateLimitRuleToJson).collect(Collectors.toList())))
        .onSuccess(ctx::json)
        .onFailure(err -> ctx.fail(500));
    });
    router.post("/api/admin/rate-limit-rules").handler(ctx -> {
      JsonObject b = ctx.body().asJsonObject();
      RateLimitRule r = buildRateLimitRule(b);
      Promise<RateLimitRule> p = Promise.promise();
      repo.save(r, p);
      p.future()
        .onSuccess(v -> ctx.response().setStatusCode(201).end(rateLimitRuleToJson(v).encode()))
        .onFailure(err -> ctx.fail(500));
    });
    router.put("/api/admin/rate-limit-rules/:id").handler(ctx -> {
      JsonObject b = ctx.body().asJsonObject();
      RateLimitRule r = buildRateLimitRule(b);
      r.setId(parseLong(ctx, "id"));
      Promise<RateLimitRule> p = Promise.promise();
      repo.update(r, p);
      p.future()
        .onSuccess(v -> ctx.json(rateLimitRuleToJson(v)))
        .onFailure(err -> notFoundOrFail(ctx, err));
    });
    router.delete("/api/admin/rate-limit-rules/:id").handler(ctx -> {
      Promise<Void> p = Promise.promise();
      repo.softDelete(parseLong(ctx, "id"), p);
      p.future()
        .onSuccess(v -> ctx.response().setStatusCode(204).end())
        .onFailure(err -> notFoundOrFail(ctx, err));
    });
  }

  private void registerBlacklistRoutes(Router router) {
    BlacklistRepository repo = new BlacklistRepository(mysqlPool);
    router.get("/api/admin/blacklist").handler(ctx -> {
      Promise<List<Blacklist>> p = Promise.promise();
      repo.findAll(p);
      p.future()
        .map(list -> new JsonArray(list.stream().map(this::blacklistToJson).collect(Collectors.toList())))
        .onSuccess(ctx::json)
        .onFailure(err -> ctx.fail(500));
    });
    router.post("/api/admin/blacklist").handler(ctx -> {
      JsonObject b = ctx.body().asJsonObject();
      Blacklist e = new Blacklist();
      e.setTargetType(b.getString("targetType"));
      e.setTargetValue(b.getString("targetValue"));
      e.setReason(b.getString("reason"));
      String expires = b.getString("expiresAt");
      e.setExpiresAt(expires != null ? LocalDateTime.parse(expires) : null);
      Promise<Blacklist> p = Promise.promise();
      repo.save(e, p);
      p.future()
        .onSuccess(v -> ctx.response().setStatusCode(201).end(blacklistToJson(v).encode()))
        .onFailure(err -> ctx.fail(500));
    });
    router.delete("/api/admin/blacklist/:id").handler(ctx -> {
      Promise<Void> p = Promise.promise();
      repo.softDelete(parseLong(ctx, "id"), p);
      p.future()
        .onSuccess(v -> ctx.response().setStatusCode(204).end())
        .onFailure(err -> notFoundOrFail(ctx, err));
    });
  }

  private void registerWhitelistRoutes(Router router) {
    WhitelistRepository repo = new WhitelistRepository(mysqlPool);
    router.get("/api/admin/whitelist").handler(ctx -> {
      Promise<List<Whitelist>> p = Promise.promise();
      repo.findAll(p);
      p.future()
        .map(list -> new JsonArray(list.stream().map(this::whitelistToJson).collect(Collectors.toList())))
        .onSuccess(ctx::json)
        .onFailure(err -> ctx.fail(500));
    });
    router.post("/api/admin/whitelist").handler(ctx -> {
      JsonObject b = ctx.body().asJsonObject();
      Whitelist e = new Whitelist();
      e.setTargetType(b.getString("targetType"));
      e.setTargetValue(b.getString("targetValue"));
      e.setDescription(b.getString("description"));
      Promise<Whitelist> p = Promise.promise();
      repo.save(e, p);
      p.future()
        .onSuccess(v -> ctx.response().setStatusCode(201).end(whitelistToJson(v).encode()))
        .onFailure(err -> ctx.fail(500));
    });
    router.delete("/api/admin/whitelist/:id").handler(ctx -> {
      Promise<Void> p = Promise.promise();
      repo.softDelete(parseLong(ctx, "id"), p);
      p.future()
        .onSuccess(v -> ctx.response().setStatusCode(204).end())
        .onFailure(err -> notFoundOrFail(ctx, err));
    });
  }

  private void registerLogRoutes(Router router) {
    router.get("/api/admin/logs").handler(ctx ->
      ctx.json(new JsonObject().put("data", new JsonArray()).put("total", 0))
    );
    router.get("/api/admin/logs/export").handler(ctx ->
      ctx.response().setStatusCode(501).end(jsonMessage("Not implemented"))
    );
  }

  private Long parseLong(RoutingContext ctx, String param) {
    try {
      return Long.valueOf(ctx.pathParam(param));
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private String jsonMessage(String message) {
    return new JsonObject().put("message", message).encode();
  }

  private void notFoundOrFail(RoutingContext ctx, Throwable err) {
    String msg = err.getMessage();
    if (msg != null && msg.toLowerCase().contains("not found")) {
      ctx.response().setStatusCode(404).end(jsonMessage(msg));
    } else {
      ctx.fail(500);
    }
  }

  private String dt(LocalDateTime d) {
    return d != null ? d.toString() : null;
  }

  private JsonObject routeToJson(GatewayRoute r) {
    return new JsonObject()
      .put("id", r.getId())
      .put("name", r.getName())
      .put("pathPattern", r.getPathPattern())
      .put("httpMethod", r.getHttpMethod())
      .put("serviceId", r.getServiceId())
      .put("authRequired", r.getAuthRequired())
      .put("rateLimitEnabled", r.getRateLimitEnabled())
      .put("priority", r.getPriority())
      .put("deleted", r.getDeleted())
      .put("createdAt", dt(r.getCreatedAt()))
      .put("updatedAt", dt(r.getUpdatedAt()));
  }

  private JsonObject serviceToJson(GatewayService s) {
    return new JsonObject()
      .put("id", s.getId())
      .put("name", s.getName())
      .put("baseUrl", s.getBaseUrl())
      .put("healthCheckUrl", s.getHealthCheckUrl())
      .put("healthCheckInterval", s.getHealthCheckInterval())
      .put("maxConnections", s.getMaxConnections())
      .put("timeoutMs", s.getTimeoutMs())
      .put("deleted", s.getDeleted())
      .put("createdAt", dt(s.getCreatedAt()))
      .put("updatedAt", dt(s.getUpdatedAt()));
  }

  private JsonObject instanceToJson(ServiceInstance i) {
    return new JsonObject()
      .put("id", i.getId())
      .put("serviceId", i.getServiceId())
      .put("host", i.getHost())
      .put("port", i.getPort())
      .put("weight", i.getWeight())
      .put("status", Boolean.TRUE.equals(i.getHealthy()) ? "HEALTHY" : "UNHEALTHY")
      .put("active", i.getEnabled())
      .put("lastHeartbeat", dt(i.getLastHeartbeat()))
      .put("deleted", i.getDeleted())
      .put("createdAt", dt(i.getCreatedAt()))
      .put("updatedAt", dt(i.getUpdatedAt()));
  }

  private JsonObject apiKeyToJson(ApiKey k) {
    return new JsonObject()
      .put("id", k.getId())
      .put("keyValue", k.getKeyValue())
      .put("name", k.getName())
      .put("userId", k.getUserId())
      .put("rateLimitPerMinute", k.getRateLimitPerMinute())
      .put("active", k.getActive())
      .put("deleted", k.getDeleted())
      .put("expiresAt", dt(k.getExpiresAt()))
      .put("createdAt", dt(k.getCreatedAt()))
      .put("updatedAt", dt(k.getUpdatedAt()));
  }

  private JsonObject rateLimitRuleToJson(RateLimitRule r) {
    return new JsonObject()
      .put("id", r.getId())
      .put("name", r.getName())
      .put("limitType", r.getLimitType())
      .put("requestsPerMinute", r.getRequestsPerMinute())
      .put("requestsPerHour", r.getRequestsPerHour())
      .put("requestsPerDay", r.getRequestsPerDay())
      .put("routeId", r.getRouteId())
      .put("serviceId", r.getServiceId())
      .put("maxRequests", r.getMaxRequests())
      .put("burstSize", r.getBurstSize())
      .put("deleted", r.getDeleted())
      .put("createdAt", dt(r.getCreatedAt()))
      .put("updatedAt", dt(r.getUpdatedAt()));
  }

  private JsonObject blacklistToJson(Blacklist b) {
    return new JsonObject()
      .put("id", b.getId())
      .put("targetType", b.getTargetType())
      .put("targetValue", b.getTargetValue())
      .put("reason", b.getReason())
      .put("expiresAt", dt(b.getExpiresAt()))
      .put("deleted", b.getDeleted())
      .put("createdAt", dt(b.getCreatedAt()))
      .put("updatedAt", dt(b.getUpdatedAt()));
  }

  private JsonObject whitelistToJson(Whitelist w) {
    return new JsonObject()
      .put("id", w.getId())
      .put("targetType", w.getTargetType())
      .put("targetValue", w.getTargetValue())
      .put("description", w.getDescription())
      .put("deleted", w.getDeleted())
      .put("createdAt", dt(w.getCreatedAt()))
      .put("updatedAt", dt(w.getUpdatedAt()));
  }

  private GatewayRoute buildRoute(JsonObject b) {
    GatewayRoute r = new GatewayRoute();
    r.setName(b.getString("name"));
    r.setPathPattern(b.getString("pathPattern"));
    r.setHttpMethod(b.getString("httpMethod"));
    r.setServiceId(b.getLong("serviceId"));
    r.setAuthRequired(b.getBoolean("authRequired", false));
    r.setRateLimitEnabled(b.getBoolean("rateLimitEnabled", false));
    r.setPriority(b.getInteger("priority", 0));
    return r;
  }

  private GatewayService buildService(JsonObject b) {
    GatewayService s = new GatewayService();
    s.setName(b.getString("name"));
    s.setBaseUrl(b.getString("baseUrl"));
    s.setHealthCheckUrl(b.getString("healthCheckUrl"));
    s.setHealthCheckInterval(b.getInteger("healthCheckInterval", 30));
    s.setMaxConnections(b.getInteger("maxConnections", 200));
    s.setTimeoutMs(b.getInteger("timeoutMs", 5000));
    return s;
  }

  private ServiceInstance buildInstance(JsonObject b) {
    ServiceInstance i = new ServiceInstance();
    i.setHost(b.getString("host"));
    i.setPort(b.getInteger("port", 80));
    i.setWeight(b.getInteger("weight", 100));
    i.setHealthy(b.getBoolean("healthy", true));
    i.setEnabled(b.getBoolean("enabled", true));
    return i;
  }

  private RateLimitRule buildRateLimitRule(JsonObject b) {
    RateLimitRule r = new RateLimitRule();
    r.setName(b.getString("name"));
    r.setLimitType(b.getString("limitType", "GLOBAL"));
    r.setRequestsPerMinute(b.getInteger("requestsPerMinute"));
    r.setRequestsPerHour(b.getInteger("requestsPerHour"));
    r.setRequestsPerDay(b.getInteger("requestsPerDay"));
    r.setBurstSize(b.getInteger("burstSize", 10));
    return r;
  }
}
