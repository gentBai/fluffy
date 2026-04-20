package com.halfhex.fluffy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("gateway_service")
public class GatewayService {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("base_url")
    private String baseUrl;

    @TableField("health_check_url")
    private String healthCheckUrl;

    @TableField("health_check_interval")
    private Integer healthCheckInterval;

    @TableField("max_connections")
    private Integer maxConnections;

    @TableField("timeout_ms")
    private Integer timeoutMs;

    @TableField("deleted")
    private Boolean deleted;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    public GatewayService() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getHealthCheckUrl() { return healthCheckUrl; }
    public void setHealthCheckUrl(String healthCheckUrl) { this.healthCheckUrl = healthCheckUrl; }
    public Integer getHealthCheckInterval() { return healthCheckInterval; }
    public void setHealthCheckInterval(Integer healthCheckInterval) { this.healthCheckInterval = healthCheckInterval; }
    public Integer getMaxConnections() { return maxConnections; }
    public void setMaxConnections(Integer maxConnections) { this.maxConnections = maxConnections; }
    public Integer getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(Integer timeoutMs) { this.timeoutMs = timeoutMs; }
    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

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
            GatewayService service = new GatewayService();
            service.id = this.id;
            service.name = this.name;
            service.baseUrl = this.baseUrl;
            service.healthCheckUrl = this.healthCheckUrl;
            service.healthCheckInterval = this.healthCheckInterval;
            service.maxConnections = this.maxConnections;
            service.timeoutMs = this.timeoutMs;
            service.deleted = this.deleted;
            service.createdAt = this.createdAt;
            service.updatedAt = this.updatedAt;
            return service;
        }
    }
}