package com.halfhex.fluffy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("gateway_route")
public class GatewayRoute {

    public enum HttpMethod {
        GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD
    }

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("path_pattern")
    private String pathPattern;

    @TableField("http_method")
    private String httpMethod;

    @TableField("service_id")
    private Long serviceId;

    @TableField("auth_required")
    private Boolean authRequired;

    @TableField("rate_limit_enabled")
    private Boolean rateLimitEnabled;

    @TableField("priority")
    private Integer priority;

    @TableField("deleted")
    private Boolean deleted;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    public GatewayRoute() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPathPattern() { return pathPattern; }
    public void setPathPattern(String pathPattern) { this.pathPattern = pathPattern; }
    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public Boolean getAuthRequired() { return authRequired; }
    public void setAuthRequired(Boolean authRequired) { this.authRequired = authRequired; }
    public Boolean getRateLimitEnabled() { return rateLimitEnabled; }
    public void setRateLimitEnabled(Boolean rateLimitEnabled) { this.rateLimitEnabled = rateLimitEnabled; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
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
        private String pathPattern;
        private String httpMethod;
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
        public Builder httpMethod(String httpMethod) { this.httpMethod = httpMethod; return this; }
        public Builder serviceId(Long serviceId) { this.serviceId = serviceId; return this; }
        public Builder authRequired(Boolean authRequired) { this.authRequired = authRequired; return this; }
        public Builder rateLimitEnabled(Boolean rateLimitEnabled) { this.rateLimitEnabled = rateLimitEnabled; return this; }
        public Builder priority(Integer priority) { this.priority = priority; return this; }
        public Builder deleted(Boolean deleted) { this.deleted = deleted; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public GatewayRoute build() {
            GatewayRoute route = new GatewayRoute();
            route.id = this.id;
            route.name = this.name;
            route.pathPattern = this.pathPattern;
            route.httpMethod = this.httpMethod;
            route.serviceId = this.serviceId;
            route.authRequired = this.authRequired;
            route.rateLimitEnabled = this.rateLimitEnabled;
            route.priority = this.priority;
            route.deleted = this.deleted;
            route.createdAt = this.createdAt;
            route.updatedAt = this.updatedAt;
            return route;
        }
    }
}