package com.halfhex.fluffy.security;

import io.vertx.core.Future;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.Command;
import io.vertx.redis.client.Request;
import io.vertx.redis.client.Response;

/**
 * Redis-based rate limiter using sliding window algorithm.
 *
 * <p>Implements multi-tier rate limiting with separate limits for
 * minute, hour, and day windows.
 *
 * @author fluffy
 */
public class RateLimiter {
    private final Redis redis;

    private static final String SLIDING_WINDOW_SCRIPT =
        "local key = KEYS[1] " +
        "local limit = tonumber(ARGV[1]) " +
        "local window = tonumber(ARGV[2]) " +
        "local now = tonumber(ARGV[3]) " +
        "local window_start = now - window " +
        "redis.call('ZREMRANGEBYSCORE', key, '-inf', window_start) " +
        "local count = redis.call('ZCARD', key) " +
        "if count >= limit then " +
        "  return {0, 0} " +
        "end " +
        "redis.call('ZADD', key, now, now .. '-' .. math.random()) " +
        "redis.call('EXPIRE', key, window + 1) " +
        "return {1, limit - count - 1}";

    public RateLimiter(Redis redis) {
        this.redis = redis;
    }

    public Future<RateLimitResult> isAllowed(String key, int maxRequests, int windowSeconds) {
        long now = System.currentTimeMillis() / 1000;
        String fullKey = "ratelimit:sliding:" + key;

        Request request = Request.cmd(Command.create("EVAL"))
            .arg(SLIDING_WINDOW_SCRIPT)
            .arg("1")
            .arg(fullKey)
            .arg(String.valueOf(maxRequests))
            .arg(String.valueOf(windowSeconds))
            .arg(String.valueOf(now));

        return redis.send(request)
            .map(response -> {
                Response result = response;
                boolean allowed = result.get(0).toInteger() == 1;
                int remaining = result.get(1).toInteger();
                return new RateLimitResult(allowed, remaining);
            })
            .recover(err -> {
                return Future.succeededFuture(new RateLimitResult(false, 0));
            });
    }

    public Future<RateLimitResult> checkRateLimit(RateLimitType type, String identifier, RateLimitRule rule) {
        String prefix;
        switch (type) {
            case IP:
                prefix = "ip";
                break;
            case USERNAME:
                prefix = "user";
                break;
            case GLOBAL:
            default:
                prefix = "global";
                break;
        }
        String key = prefix + ":" + identifier;

        int burstLimit = rule.getBurstSize() > 0 ? rule.getBurstSize() : rule.getRequestsPerMinute();

        return isAllowed(key + ":minute", burstLimit, 60)
            .compose(minuteResult -> {
                if (!minuteResult.isAllowed()) {
                    return Future.succeededFuture(minuteResult);
                }
                return isAllowed(key + ":hour", rule.getRequestsPerHour(), 3600)
                    .compose(hourResult -> {
                        if (!hourResult.isAllowed()) {
                            return Future.succeededFuture(hourResult);
                        }
                        return isAllowed(key + ":day", rule.getRequestsPerDay(), 86400);
                    });
            });
    }

    public static class RateLimitResult {
        private final boolean allowed;
        private final int remaining;

        public RateLimitResult(boolean allowed, int remaining) {
            this.allowed = allowed;
            this.remaining = remaining;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public int getRemaining() {
            return remaining;
        }
    }
}
