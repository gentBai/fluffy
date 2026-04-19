package com.halfhex.fluffy.entity;

import java.time.LocalDateTime;

public final class LoadBalanceStrategy {

    public enum StrategyType {
        ROUND_ROBIN, RANDOM, CONSISTENT_HASH
    }

    private final Long id;
    private final Long serviceId;
    private final StrategyType strategyType;
    private final Boolean enabled;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private LoadBalanceStrategy(Builder builder) {
        this.id = builder.id;
        this.serviceId = builder.serviceId;
        this.strategyType = builder.strategyType;
        this.enabled = builder.enabled;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public Long getId() { return id; }
    public Long getServiceId() { return serviceId; }
    public StrategyType getStrategyType() { return strategyType; }
    public Boolean getEnabled() { return enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long serviceId;
        private StrategyType strategyType;
        private Boolean enabled;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder serviceId(Long serviceId) { this.serviceId = serviceId; return this; }
        public Builder strategyType(StrategyType strategyType) { this.strategyType = strategyType; return this; }
        public Builder enabled(Boolean enabled) { this.enabled = enabled; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public LoadBalanceStrategy build() {
            return new LoadBalanceStrategy(this);
        }
    }
}
