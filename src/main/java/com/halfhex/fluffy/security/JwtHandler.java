package com.halfhex.fluffy.security;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Handles JWT token generation and validation.
 *
 * <p>Loads JWT configuration from database and provides token validation
 * with proper error handling for expired and invalid tokens.
 *
 * @author fluffy
 */
public class JwtHandler {

    private final Vertx vertx;
    private final Pool mysqlPool;
    private final AtomicReference<JwtConfig> cachedConfig = new AtomicReference<>();
    private final AtomicReference<JWTAuth> jwtAuth = new AtomicReference<>();
    private Long refreshTimerId;

    private static final long DEFAULT_REFRESH_INTERVAL_MS = 60000;

    public JwtHandler(Vertx vertx, Pool mysqlPool) {
        this.vertx = vertx;
        this.mysqlPool = mysqlPool;
    }

    public Future<Void> initialize() {
        return loadConfig()
            .compose(config -> {
                initJwtAuth(config);
                startPeriodicRefresh();
                return Future.succeededFuture();
            });
    }

    private Future<JwtConfig> loadConfig() {
        Promise<JwtConfig> promise = Promise.promise();
        mysqlPool.withConnection(conn ->
          conn.preparedQuery("SELECT * FROM jwt_config WHERE enabled = 1 LIMIT 1")
            .execute()
            .map(rowSet -> {
              if (!rowSet.iterator().hasNext()) {
                throw new RuntimeException("No enabled jwt_config found");
              }
              Row row = rowSet.iterator().next();
              return JwtConfig.builder()
                .id(row.getLong("id"))
                .issuer(row.getString("issuer"))
                .secretKey(row.getString("secret_key"))
                .algorithm(row.getString("algorithm"))
                .accessTokenTtlSec(row.getInteger("access_token_ttl_sec"))
                .refreshTokenTtlSec(row.getInteger("refresh_token_ttl_sec"))
                .enabled(row.getBoolean("enabled"))
                .build();
            })
        ).onComplete(ar -> {
          if (ar.succeeded()) promise.complete(ar.result());
          else promise.fail(ar.cause());
        });
        return promise.future();
    }

    private void initJwtAuth(JwtConfig config) {
        JWTAuthOptions options = new JWTAuthOptions()
            .addPubSecKey(new PubSecKeyOptions()
                .setAlgorithm(config.getAlgorithm())
                .setBuffer(config.getSecretKey()));
        JWTAuth auth = JWTAuth.create(vertx, options);
        jwtAuth.set(auth);
        cachedConfig.set(config);
    }

    private void startPeriodicRefresh() {
        refreshTimerId = vertx.setPeriodic(DEFAULT_REFRESH_INTERVAL_MS, id -> {
            loadConfig().onComplete(ar -> {
                if (ar.succeeded()) {
                    initJwtAuth(ar.result());
                }
            });
        });
    }

    public Future<Claims> validateToken(String token) {
        Promise<Claims> promise = Promise.promise();
        JWTAuth auth = jwtAuth.get();
        if (auth == null) {
            promise.fail("JWT auth not initialized");
            return promise.future();
        }
        auth.authenticate(new TokenCredentials(token))
            .onSuccess(user -> {
                JsonObject principal = user.principal();
                Claims claims = new Claims(
                    principal.getLong("userId"),
                    principal.getString("sub"),
                    principal.getLong("iat"),
                    principal.getLong("exp")
                );
                promise.complete(claims);
            })
            .onFailure(err -> {
                String message = err.getMessage();
                if (message != null && message.contains("Expired")) {
                    promise.fail(new TokenException("Token expired", TokenError.EXPIRED));
                } else if (message != null && message.contains("Invalid")) {
                    promise.fail(new TokenException("Invalid token", TokenError.INVALID));
                } else {
                    promise.fail(new TokenException("Token validation failed", TokenError.INVALID));
                }
            });
        return promise.future();
    }

    public Future<String> generateToken(Long userId, Map<String, Object> claims) {
        Promise<String> promise = Promise.promise();
        JwtConfig config = cachedConfig.get();
        JWTAuth auth = jwtAuth.get();
        if (auth == null || config == null) {
            promise.fail("JWT auth not initialized");
            return promise.future();
        }
        long now = Instant.now().getEpochSecond();
        long exp = now + config.getAccessTokenTtlSec();
        JsonObject jwtClaims = new JsonObject()
            .put("userId", userId)
            .put("iat", now)
            .put("exp", exp)
            .put("iss", config.getIssuer());
        for (Map.Entry<String, Object> entry : claims.entrySet()) {
            jwtClaims.put(entry.getKey(), entry.getValue());
        }
        String token = auth.generateToken(jwtClaims);
        promise.complete(token);
        return promise.future();
    }

    public Future<String> generateRefreshToken(Long userId) {
        Promise<String> promise = Promise.promise();
        JwtConfig config = cachedConfig.get();
        JWTAuth auth = jwtAuth.get();
        if (auth == null || config == null) {
            promise.fail("JWT auth not initialized");
            return promise.future();
        }
        long now = Instant.now().getEpochSecond();
        long exp = now + config.getRefreshTokenTtlSec();
        JsonObject jwtClaims = new JsonObject()
            .put("userId", userId)
            .put("type", "refresh")
            .put("iat", now)
            .put("exp", exp)
            .put("iss", config.getIssuer());
        String token = auth.generateToken(jwtClaims);
        promise.complete(token);
        return promise.future();
    }

    public void close() {
        if (refreshTimerId != null) {
            vertx.cancelTimer(refreshTimerId);
        }
    }

    public static class Claims {
        private final Long userId;
        private final String subject;
        private final Long issuedAt;
        private final Long expiration;

        public Claims(Long userId, String subject, Long issuedAt, Long expiration) {
            this.userId = userId;
            this.subject = subject;
            this.issuedAt = issuedAt;
            this.expiration = expiration;
        }

        public Long getUserId() { return userId; }
        public String getSubject() { return subject; }
        public Long getIssuedAt() { return issuedAt; }
        public Long getExpiration() { return expiration; }
    }

    public static class TokenException extends RuntimeException {
        private final TokenError error;

        public TokenException(String message, TokenError error) {
            super(message);
            this.error = error;
        }

        public TokenError getError() { return error; }
    }

    public enum TokenError {
        EXPIRED,
        INVALID,
        MISSING
    }
}