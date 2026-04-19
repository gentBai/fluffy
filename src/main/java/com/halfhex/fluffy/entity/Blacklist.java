package com.halfhex.fluffy.entity;

import java.time.LocalDateTime;

public final class Blacklist {

    public enum TargetType {
        IP, USER, API_KEY
    }

    private final Long id;
    private final String targetValue;
    private final TargetType targetType;
    private final String reason;
    private final LocalDateTime expiresAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Blacklist(Builder builder) {
        this.id = builder.id;
        this.targetValue = builder.targetValue;
        this.targetType = builder.targetType;
        this.reason = builder.reason;
        this.expiresAt = builder.expiresAt;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public Long getId() { return id; }
    public String getTargetValue() { return targetValue; }
    public TargetType getTargetType() { return targetType; }
    public String getReason() { return reason; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String targetValue;
        private TargetType targetType;
        private String reason;
        private LocalDateTime expiresAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder targetValue(String targetValue) { this.targetValue = targetValue; return this; }
        public Builder targetType(TargetType targetType) { this.targetType = targetType; return this; }
        public Builder reason(String reason) { this.reason = reason; return this; }
        public Builder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Blacklist build() {
            return new Blacklist(this);
        }
    }
}
