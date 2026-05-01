package com.halfhex.fluffy.security;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.Command;
import io.vertx.redis.client.Request;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.time.LocalDateTime;

public class ApiKeyHandler {

    private static final String CACHE_PREFIX = "apikey:";
    private static final long CACHE_TTL_SECONDS = 300;

    private final Pool mysqlPool;
    private final Redis redis;

    public ApiKeyHandler(Pool mysqlPool, Redis redis) {
        this.mysqlPool = mysqlPool;
        this.redis = redis;
    }

    public Future<ApiKey> validateApiKey(String keyValue) {
        if (keyValue == null || keyValue.isBlank()) {
            return Future.failedFuture("API key is required");
        }

        String cacheKey = CACHE_PREFIX + keyValue;

        return redis.send(Request.cmd(Command.GET).arg(cacheKey))
            .compose(cacheResult -> {
                if (cacheResult != null) {
                    return Future.succeededFuture(fromCache(cacheResult));
                }
                return validateFromDatabase(keyValue)
                    .compose(apiKey -> cacheApiKey(cacheKey, apiKey).map(apiKey));
            });
    }

    private Future<ApiKey> validateFromDatabase(String keyValue) {
        return mysqlPool.withConnection(conn ->
          conn.preparedQuery(
              "SELECT id, key_value, name, user_id, route_ids, rate_limit_per_minute, deleted, expires_at " +
              "FROM api_key WHERE key_value = ?"
          ).execute(Tuple.of(keyValue))
          .map(rowSet -> {
            if (!rowSet.iterator().hasNext()) {
              throw new RuntimeException("API key not found");
            }
            Row row = rowSet.iterator().next();
            ApiKey apiKey = mapRowToApiKey(row);
            if (!apiKey.isValid()) {
              throw new RuntimeException("API key is invalid, expired, or disabled");
            }
            return apiKey;
          })
        );
    }

    private Future<Void> cacheApiKey(String cacheKey, ApiKey apiKey) {
        return redis.send(Request.cmd(Command.SETEX)
            .arg(cacheKey)
            .arg(CACHE_TTL_SECONDS)
            .arg(toCache(apiKey))).mapEmpty();
    }

    private ApiKey fromCache(Object cacheResult) {
        JsonObject json = new JsonObject(cacheResult.toString());
        return ApiKey.builder()
            .id(json.getLong("id"))
            .keyValue(json.getString("keyValue"))
            .name(json.getString("name"))
            .userId(json.getLong("userId"))
            .routeIds(json.getJsonArray("routeIds"))
            .rateLimitPerMinute(json.getInteger("rateLimitPerMinute"))
            .deleted(json.getBoolean("deleted"))
            .active(json.getBoolean("active"))
            .expiresAt(json.getString("expiresAt") != null ?
                LocalDateTime.parse(json.getString("expiresAt")) : null)
            .build();
    }

    private String toCache(ApiKey apiKey) {
        JsonObject json = new JsonObject()
            .put("id", apiKey.getId())
            .put("keyValue", apiKey.getKeyValue())
            .put("name", apiKey.getName())
            .put("userId", apiKey.getUserId())
            .put("routeIds", apiKey.getRouteIds())
            .put("rateLimitPerMinute", apiKey.getRateLimitPerMinute())
            .put("deleted", apiKey.getDeleted())
            .put("expiresAt", apiKey.getExpiresAt() != null ? apiKey.getExpiresAt().toString() : null);
        return json.encode();
    }

    private ApiKey mapRowToApiKey(Row row) {
        return ApiKey.builder()
            .id(row.getLong("id"))
            .keyValue(row.getString("key_value"))
            .name(row.getString("name"))
            .userId(row.getLong("user_id"))
            .routeIds(row.getJsonArray("route_ids"))
            .rateLimitPerMinute(row.getInteger("rate_limit_per_minute"))
            .deleted(row.getBoolean("deleted"))
            .active(row.getBoolean("active"))
            .expiresAt(row.getLocalDateTime("expires_at"))
            .build();
    }

    public static String extractApiKeyFromHeader(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return null;
        }
        if (headerValue.startsWith("Bearer ")) {
            return headerValue.substring(7).trim();
        }
        return headerValue.trim();
    }
}