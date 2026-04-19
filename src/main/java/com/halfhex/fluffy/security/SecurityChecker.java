package com.halfhex.fluffy.security;

import io.vertx.core.Future;
import io.vertx.redis.client.Command;
import io.vertx.redis.client.Request;
import io.vertx.redis.client.Redis;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Tuple;

public class SecurityChecker {

    private static final String CACHE_PREFIX = "security:";
    private static final long CACHE_TTL_SECONDS = 60;

    private final Pool mysqlPool;
    private final Redis redis;

    public SecurityChecker(Pool mysqlPool, Redis redis) {
        this.mysqlPool = mysqlPool;
        this.redis = redis;
    }

    public Future<Boolean> isBlacklisted(TargetType type, String value) {
        String cacheKey = CACHE_PREFIX + "bl:" + type.name().toLowerCase() + ":" + value;

        return redis.send(Request.cmd(Command.create("GET")).arg(cacheKey))
            .compose(cached -> {
                if (cached != null) {
                    return Future.succeededFuture("true".equals(cached.toString()));
                }
                return queryBlacklist(type, value)
                    .compose(result -> {
                        redis.send(Request.cmd(Command.create("SETEX"))
                            .arg(cacheKey)
                            .arg(String.valueOf(CACHE_TTL_SECONDS))
                            .arg(result ? "true" : "false"));
                        return Future.succeededFuture(result);
                    });
            });
    }

    public Future<Boolean> isWhitelisted(TargetType type, String value) {
        String cacheKey = CACHE_PREFIX + "wl:" + type.name().toLowerCase() + ":" + value;

        return redis.send(Request.cmd(Command.create("GET")).arg(cacheKey))
            .compose(cached -> {
                if (cached != null) {
                    return Future.succeededFuture("true".equals(cached.toString()));
                }
                return queryWhitelist(type, value)
                    .compose(result -> {
                        redis.send(Request.cmd(Command.create("SETEX"))
                            .arg(cacheKey)
                            .arg(String.valueOf(CACHE_TTL_SECONDS))
                            .arg(result ? "true" : "false"));
                        return Future.succeededFuture(result);
                    });
            });
    }

    public Future<SecurityCheckResult> checkAccess(String clientIp, Long userId, String apiKeyId) {
        return checkWhitelistFirst(clientIp, userId, apiKeyId)
            .compose(whitelisted -> {
                if (whitelisted) {
                    return Future.succeededFuture(SecurityCheckResult.BYPASSED);
                }
                return checkBlacklist(clientIp, userId, apiKeyId);
            });
    }

    private Future<Boolean> checkWhitelistFirst(String clientIp, Long userId, String apiKeyId) {
        return isWhitelisted(TargetType.IP, clientIp)
            .compose(ipResult -> {
                if (Boolean.TRUE.equals(ipResult)) {
                    return Future.succeededFuture(true);
                }
                if (userId != null) {
                    return isWhitelisted(TargetType.USER, userId.toString())
                        .compose(userResult -> {
                            if (Boolean.TRUE.equals(userResult)) {
                                return Future.succeededFuture(true);
                            }
                            if (apiKeyId != null) {
                                return isWhitelisted(TargetType.API_KEY, apiKeyId);
                            }
                            return Future.succeededFuture(false);
                        });
                }
                if (apiKeyId != null) {
                    return isWhitelisted(TargetType.API_KEY, apiKeyId);
                }
                return Future.succeededFuture(false);
            });
    }

    private Future<SecurityCheckResult> checkBlacklist(String clientIp, Long userId, String apiKeyId) {
        return isBlacklisted(TargetType.IP, clientIp)
            .compose(ipResult -> {
                if (Boolean.TRUE.equals(ipResult)) {
                    return Future.succeededFuture(SecurityCheckResult.BLOCKED);
                }
                if (userId != null) {
                    return isBlacklisted(TargetType.USER, userId.toString())
                        .compose(userResult -> {
                            if (Boolean.TRUE.equals(userResult)) {
                                return Future.succeededFuture(SecurityCheckResult.BLOCKED);
                            }
                            if (apiKeyId != null) {
                                return isBlacklisted(TargetType.API_KEY, apiKeyId)
                                    .map(apiResult -> Boolean.TRUE.equals(apiResult) ? SecurityCheckResult.BLOCKED : SecurityCheckResult.ALLOWED);
                            }
                            return Future.succeededFuture(SecurityCheckResult.ALLOWED);
                        });
                }
                if (apiKeyId != null) {
                    return isBlacklisted(TargetType.API_KEY, apiKeyId)
                        .map(apiResult -> Boolean.TRUE.equals(apiResult) ? SecurityCheckResult.BLOCKED : SecurityCheckResult.ALLOWED);
                }
                return Future.succeededFuture(SecurityCheckResult.ALLOWED);
            });
    }

    private Future<Boolean> queryBlacklist(TargetType type, String value) {
        String sql = "SELECT 1 FROM blacklist WHERE target_type = ? AND target_value = ? " +
                     "AND (expires_at IS NULL OR expires_at > NOW()) LIMIT 1";

        return mysqlPool.preparedQuery(sql)
            .execute(Tuple.of(type.name(), value))
            .map(rows -> rows.rowCount() > 0);
    }

    private Future<Boolean> queryWhitelist(TargetType type, String value) {
        String sql = "SELECT 1 FROM whitelist WHERE target_type = ? AND target_value = ? " +
                     "AND (expires_at IS NULL OR expires_at > NOW()) LIMIT 1";

        return mysqlPool.preparedQuery(sql)
            .execute(Tuple.of(type.name(), value))
            .map(rows -> rows.rowCount() > 0);
    }
}