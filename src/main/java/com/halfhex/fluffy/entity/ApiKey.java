package com.halfhex.fluffy.entity;

import java.time.LocalDateTime;

public final class ApiKey {

    private final Long id;
    private final String key;
    private final String name;
    private final Long userId;
    private final Boolean enabled;
    private final LocalDateTime expiresAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ApiKey(Builder builder) {
        this.id = builder.id;
        this.key = builder.key;
        this.name = builder.name;
        this.userId = builder.userId;
        this.enabled = builder.enabled;
        this.expiresAt = builder.expiresAt;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public Long getId() { return id; }
    public String getKey() { return key; }
    public String getName() { return name; }
    public Long getUserId() { return userId; }
    public Boolean getEnabled() { return enabled; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String key;
        private String name;
        private Long userId;
        private Boolean enabled;
        private LocalDateTime expiresAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder key(String key) { this.key = key; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder enabled(Boolean enabled) { this.enabled = enabled; return this; }
        public Builder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public ApiKey build() {
            return new ApiKey(this);
        }
    }
}
