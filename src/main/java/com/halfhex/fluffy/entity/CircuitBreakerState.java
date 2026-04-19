package com.halfhex.fluffy.entity;

import java.time.LocalDateTime;

public final class CircuitBreakerState {

    public enum State {
        CLOSED, OPEN, HALF_OPEN
    }

    private final Long id;
    private final Long serviceId;
    private final State state;
    private final Integer failureCount;
    private final Integer successCount;
    private final LocalDateTime lastFailureTime;
    private final LocalDateTime openedAt;
    private final LocalDateTime closedAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private CircuitBreakerState(Builder builder) {
        this.id = builder.id;
        this.serviceId = builder.serviceId;
        this.state = builder.state;
        this.failureCount = builder.failureCount;
        this.successCount = builder.successCount;
        this.lastFailureTime = builder.lastFailureTime;
        this.openedAt = builder.openedAt;
        this.closedAt = builder.closedAt;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public Long getId() { return id; }
    public Long getServiceId() { return serviceId; }
    public State getState() { return state; }
    public Integer getFailureCount() { return failureCount; }
    public Integer getSuccessCount() { return successCount; }
    public LocalDateTime getLastFailureTime() { return lastFailureTime; }
    public LocalDateTime getOpenedAt() { return openedAt; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long serviceId;
        private State state;
        private Integer failureCount;
        private Integer successCount;
        private LocalDateTime lastFailureTime;
        private LocalDateTime openedAt;
        private LocalDateTime closedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder serviceId(Long serviceId) { this.serviceId = serviceId; return this; }
        public Builder state(State state) { this.state = state; return this; }
        public Builder failureCount(Integer failureCount) { this.failureCount = failureCount; return this; }
        public Builder successCount(Integer successCount) { this.successCount = successCount; return this; }
        public Builder lastFailureTime(LocalDateTime lastFailureTime) { this.lastFailureTime = lastFailureTime; return this; }
        public Builder openedAt(LocalDateTime openedAt) { this.openedAt = openedAt; return this; }
        public Builder closedAt(LocalDateTime closedAt) { this.closedAt = closedAt; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public CircuitBreakerState build() {
            return new CircuitBreakerState(this);
        }
    }
}
