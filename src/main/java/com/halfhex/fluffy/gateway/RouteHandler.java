package com.halfhex.fluffy.gateway;

import com.halfhex.fluffy.entity.GatewayRoute;
import com.halfhex.fluffy.entity.GatewayRoute.HttpMethod;
import com.halfhex.fluffy.repository.RouteRepository;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLPool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class RouteHandler {

  private final RouteRepository routeRepository;
  private final Vertx vertx;
  private final List<GatewayRoute> routeCache;
  private final AtomicLong cacheVersion;
  private long periodicTimerId;

  public RouteHandler(MySQLPool mysqlPool, Vertx vertx) {
    this.routeRepository = new RouteRepository(mysqlPool);
    this.vertx = vertx;
    this.routeCache = Collections.synchronizedList(new ArrayList<>());
    this.cacheVersion = new AtomicLong(0);
  }

  public Future<GatewayRoute> matchRoute(String path, String method) {
    Promise<GatewayRoute> promise = Promise.promise();

    List<GatewayRoute> currentCache = new ArrayList<>(routeCache);

    GatewayRoute matched = findBestMatch(currentCache, path, method);

    if (matched != null) {
      promise.complete(matched);
    } else {
      refreshCache().onComplete(ar -> {
        if (ar.succeeded()) {
          GatewayRoute newMatched = findBestMatch(new ArrayList<>(routeCache), path, method);
          promise.complete(newMatched);
        } else {
          promise.fail(ar.cause());
        }
      });
    }

    return promise.future();
  }

  public Future<Void> refreshCache() {
    Promise<List<GatewayRoute>> repoPromise = Promise.promise();

    routeRepository.findAllEnabled(repoPromise);

    return repoPromise.future().map(routes -> {
      routeCache.clear();
      routeCache.addAll(routes);
      cacheVersion.incrementAndGet();
      return null;
    });
  }

  public void startPeriodicRefresh() {
    refreshCache();
    periodicTimerId = vertx.setPeriodic(30_000, id -> {
      refreshCache();
    });
  }

  public void stopPeriodicRefresh() {
    if (periodicTimerId > 0) {
      vertx.cancelTimer(periodicTimerId);
      periodicTimerId = 0;
    }
  }

  private GatewayRoute findBestMatch(List<GatewayRoute> routes, String path, String method) {
    GatewayRoute bestMatch = null;
    int highestPriority = -1;

    for (GatewayRoute route : routes) {
      if (pathMatches(route.getPathPattern(), path)) {
        HttpMethod routeMethod = route.getHttpMethod();
        boolean methodMatches = routeMethod == null || routeMethod.name().equalsIgnoreCase(method);

        if (methodMatches) {
          int priority = route.getPriority() != null ? route.getPriority() : 0;
          if (priority > highestPriority) {
            highestPriority = priority;
            bestMatch = route;
          }
        }
      }
    }

    return bestMatch;
  }

  private boolean pathMatches(String pattern, String path) {
    if (pattern == null || path == null) {
      return false;
    }

    if (pattern.equals(path)) {
      return true;
    }

    if (pattern.endsWith("/*")) {
      String prefix = pattern.substring(0, pattern.length() - 2);
      return path.startsWith(prefix) || path.equals(prefix.substring(0, prefix.length() - 1));
    }

    if (pattern.endsWith("/**")) {
      String prefix = pattern.substring(0, pattern.length() - 3);
      return path.startsWith(prefix);
    }

    if (pattern.contains("*")) {
      String regex = pattern
        .replace(".", "\\.")
        .replace("**", ".*")
        .replace("*", "[^/]*");
      return path.matches(regex);
    }

    return false;
  }
}
