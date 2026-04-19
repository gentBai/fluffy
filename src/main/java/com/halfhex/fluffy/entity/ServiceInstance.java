package com.halfhex.fluffy.entity;

import java.time.LocalDateTime;

public final class ServiceInstance {

    public enum Status {
        HEALTHY, UNHEALTHY, DOWN
    }

    private final Long id;
    private final Long serviceId;
    private final String host;
    private final Integer port;
    private final Status status;
    private final Boolean enabled;
    private final LocalDateTime lastHeartbeat;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ServiceInstance(Builder builder) {
        this.id = builder.id;
        this.serviceId = builder.serviceId;
        this.host = builder.host;
        this.port = builder.port;
        this.status = builder.status;
        this.enabled = builder.enabled;
        this.lastHeartbeat = builder.lastHeartbeat;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public Long getId() { return id; }
    public Long getServiceId() { return serviceId; }
    public String getHost() { return host; }
    public Integer getPort() { return port; }
    public Status getStatus() { return status; }
    public Boolean getEnabled() { return enabled; }
    public LocalDateTime getLastHeartbeat() { return lastHeartbeat; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long serviceId;
        private String host;
        private Integer port;
        private Status status;
        private Boolean enabled;
        private LocalDateTime lastHeartbeat;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder serviceId(Long serviceId) { this.serviceId = serviceId; return this; }
        public Builder host(String host) { this.host = host; return this; }
        public Builder port(Integer port) { this.port = port; return this; }
        public Builder status(Status status) { this.status = status; return this; }
        public Builder enabled(Boolean enabled) { this.enabled = enabled; return this; }
        public Builder lastHeartbeat(LocalDateTime lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public ServiceInstance build() {
            return new ServiceInstance(this);
        }
    }
}
