package com.halfhex.fluffy.security;

import io.vertx.core.json.JsonArray;

import java.time.LocalDateTime;

public final class ApiKey {

    private final Long id;
    private final String keyValue;
    private final String name;
    private final Long userId;
    private final JsonArray routeIds;
    private final Integer rateLimitPerMinute;
    private final Boolean deleted;
    private final LocalDateTime expiresAt;

    private ApiKey(Builder builder) {
        this.id = builder.id;
        this.keyValue = builder.keyValue;
        this.name = builder.name;
        this.userId = builder.userId;
        this.routeIds = builder.routeIds;
        this.rateLimitPerMinute = builder.rateLimitPerMinute;
        this.deleted = builder.deleted;
        this.expiresAt = builder.expiresAt;
    }

    public Long getId() { return id; }
    public String getKeyValue() { return keyValue; }
    public String getName() { return name; }
    public Long getUserId() { return userId; }
    public JsonArray getRouteIds() { return routeIds; }
    public Integer getRateLimitPerMinute() { return rateLimitPerMinute; }
    public Boolean getDeleted() { return deleted; }
    public LocalDateTime getExpiresAt() { return expiresAt; }

    public boolean isValid() {
        if (Boolean.TRUE.equals(deleted)) return false;
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) return false;
        return true;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String keyValue;
        private String name;
        private Long userId;
        private JsonArray routeIds;
        private Integer rateLimitPerMinute;
        private Boolean deleted;
        private LocalDateTime expiresAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder keyValue(String keyValue) { this.keyValue = keyValue; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder routeIds(JsonArray routeIds) { this.routeIds = routeIds; return this; }
        public Builder rateLimitPerMinute(Integer rateLimitPerMinute) { this.rateLimitPerMinute = rateLimitPerMinute; return this; }
        public Builder deleted(Boolean deleted) { this.deleted = deleted; return this; }
        public Builder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }

        public ApiKey build() {
            return new ApiKey(this);
        }
    }
}