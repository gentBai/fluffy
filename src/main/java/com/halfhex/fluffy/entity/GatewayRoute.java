package com.halfhex.fluffy.entity;

import java.time.LocalDateTime;

public final class GatewayRoute {

  // Route status for soft delete

    public enum HttpMethod {
        GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD
    }

    private final Long id;
    private final String name;
    private final String pathPattern;
    private final HttpMethod httpMethod;
    private final Long serviceId;
    private final Boolean authRequired;
    private final Boolean rateLimitEnabled;
    private final Integer priority;
    private final Boolean deleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private GatewayRoute(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.pathPattern = builder.pathPattern;
        this.httpMethod = builder.httpMethod;
        this.serviceId = builder.serviceId;
        this.authRequired = builder.authRequired;
        this.rateLimitEnabled = builder.rateLimitEnabled;
        this.priority = builder.priority;
        this.deleted = builder.deleted;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPathPattern() { return pathPattern; }
    public HttpMethod getHttpMethod() { return httpMethod; }
    public Long getServiceId() { return serviceId; }
    public Boolean getAuthRequired() { return authRequired; }
    public Boolean getRateLimitEnabled() { return rateLimitEnabled; }
    public Integer getPriority() { return priority; }
    public Boolean getDeleted() { return deleted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String name;
        private String pathPattern;
        private HttpMethod httpMethod;
        private Long serviceId;
        private Boolean authRequired;
        private Boolean rateLimitEnabled;
        private Integer priority;
        private Boolean deleted;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder pathPattern(String pathPattern) { this.pathPattern = pathPattern; return this; }
        public Builder httpMethod(HttpMethod httpMethod) { this.httpMethod = httpMethod; return this; }
        public Builder serviceId(Long serviceId) { this.serviceId = serviceId; return this; }
        public Builder authRequired(Boolean authRequired) { this.authRequired = authRequired; return this; }
        public Builder rateLimitEnabled(Boolean rateLimitEnabled) { this.rateLimitEnabled = rateLimitEnabled; return this; }
        public Builder priority(Integer priority) { this.priority = priority; return this; }
        public Builder deleted(Boolean deleted) { this.deleted = deleted; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public GatewayRoute build() {
            return new GatewayRoute(this);
        }
    }
}
