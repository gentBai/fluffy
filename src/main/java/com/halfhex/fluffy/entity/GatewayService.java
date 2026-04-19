package com.halfhex.fluffy.entity;

import java.time.LocalDateTime;

public final class GatewayService {

    private final Long id;
    private final String name;
    private final String baseUrl;
    private final String healthCheckUrl;
    private final Integer healthCheckInterval;
    private final Integer maxConnections;
    private final Integer timeoutMs;
    private final Boolean deleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private GatewayService(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.baseUrl = builder.baseUrl;
        this.healthCheckUrl = builder.healthCheckUrl;
        this.healthCheckInterval = builder.healthCheckInterval;
        this.maxConnections = builder.maxConnections;
        this.timeoutMs = builder.timeoutMs;
        this.deleted = builder.deleted;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getBaseUrl() { return baseUrl; }
    public String getHealthCheckUrl() { return healthCheckUrl; }
    public Integer getHealthCheckInterval() { return healthCheckInterval; }
    public Integer getMaxConnections() { return maxConnections; }
    public Integer getTimeoutMs() { return timeoutMs; }
    public Boolean getDeleted() { return deleted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String name;
        private String baseUrl;
        private String healthCheckUrl;
        private Integer healthCheckInterval;
        private Integer maxConnections;
        private Integer timeoutMs;
        private Boolean deleted;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder baseUrl(String baseUrl) { this.baseUrl = baseUrl; return this; }
        public Builder healthCheckUrl(String healthCheckUrl) { this.healthCheckUrl = healthCheckUrl; return this; }
        public Builder healthCheckInterval(Integer healthCheckInterval) { this.healthCheckInterval = healthCheckInterval; return this; }
        public Builder maxConnections(Integer maxConnections) { this.maxConnections = maxConnections; return this; }
        public Builder timeoutMs(Integer timeoutMs) { this.timeoutMs = timeoutMs; return this; }
        public Builder deleted(Boolean deleted) { this.deleted = deleted; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public GatewayService build() {
            return new GatewayService(this);
        }
    }
}
