package com.halfhex.fluffy.entity;

import java.time.LocalDateTime;

public final class RateLimitRule {

    public enum LimitType {
        IP, USERNAME, GLOBAL
    }

    private final Long id;
    private final Long routeId;
    private final LimitType limitType;
    private final Integer maxRequests;
    private final Integer windowSeconds;
    private final Boolean enabled;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private RateLimitRule(Builder builder) {
        this.id = builder.id;
        this.routeId = builder.routeId;
        this.limitType = builder.limitType;
        this.maxRequests = builder.maxRequests;
        this.windowSeconds = builder.windowSeconds;
        this.enabled = builder.enabled;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public Long getId() { return id; }
    public Long getRouteId() { return routeId; }
    public LimitType getLimitType() { return limitType; }
    public Integer getMaxRequests() { return maxRequests; }
    public Integer getWindowSeconds() { return windowSeconds; }
    public Boolean getEnabled() { return enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long routeId;
        private LimitType limitType;
        private Integer maxRequests;
        private Integer windowSeconds;
        private Boolean enabled;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder routeId(Long routeId) { this.routeId = routeId; return this; }
        public Builder limitType(LimitType limitType) { this.limitType = limitType; return this; }
        public Builder maxRequests(Integer maxRequests) { this.maxRequests = maxRequests; return this; }
        public Builder windowSeconds(Integer windowSeconds) { this.windowSeconds = windowSeconds; return this; }
        public Builder enabled(Boolean enabled) { this.enabled = enabled; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public RateLimitRule build() {
            return new RateLimitRule(this);
        }
    }
}
