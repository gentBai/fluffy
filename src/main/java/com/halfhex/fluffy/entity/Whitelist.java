package com.halfhex.fluffy.entity;

import java.time.LocalDateTime;

public final class Whitelist {

    private final Long id;
    private final String targetValue;
    private final String targetType;
    private final String description;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Whitelist(Builder builder) {
        this.id = builder.id;
        this.targetValue = builder.targetValue;
        this.targetType = builder.targetType;
        this.description = builder.description;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public Long getId() { return id; }
    public String getTargetValue() { return targetValue; }
    public String getTargetType() { return targetType; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String targetValue;
        private String targetType;
        private String description;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder targetValue(String targetValue) { this.targetValue = targetValue; return this; }
        public Builder targetType(String targetType) { this.targetType = targetType; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Whitelist build() {
            return new Whitelist(this);
        }
    }
}
