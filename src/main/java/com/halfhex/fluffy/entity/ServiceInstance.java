package com.halfhex.fluffy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("service_instance")
public class ServiceInstance {

    public enum Status {
        HEALTHY, UNHEALTHY, DOWN
    }

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("service_id")
    private Long serviceId;

    @TableField("host")
    private String host;

    @TableField("port")
    private Integer port;

    @TableField("weight")
    private Integer weight;

    @TableField("healthy")
    private Boolean healthy;

    @TableField("enabled")
    private Boolean enabled;

    @TableField("last_heartbeat")
    private LocalDateTime lastHeartbeat;

    @TableField("deleted")
    private Boolean deleted;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    public ServiceInstance() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }
    public Boolean getHealthy() { return healthy; }
    public void setHealthy(Boolean healthy) { this.healthy = healthy; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getLastHeartbeat() { return lastHeartbeat; }
    public void setLastHeartbeat(LocalDateTime lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Status getStatus() {
        if (healthy == null) {
            return null;
        }
        return healthy ? Status.HEALTHY : Status.UNHEALTHY;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long serviceId;
        private String host;
        private Integer port;
        private Integer weight;
        private Boolean healthy;
        private Boolean enabled;
        private LocalDateTime lastHeartbeat;
        private Boolean deleted;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder serviceId(Long serviceId) { this.serviceId = serviceId; return this; }
        public Builder host(String host) { this.host = host; return this; }
        public Builder port(Integer port) { this.port = port; return this; }
        public Builder weight(Integer weight) { this.weight = weight; return this; }
        public Builder healthy(Boolean healthy) { this.healthy = healthy; return this; }
        public Builder status(Status status) { this.healthy = (status == Status.HEALTHY); return this; }
        public Builder enabled(Boolean enabled) { this.enabled = enabled; return this; }
        public Builder lastHeartbeat(LocalDateTime lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; return this; }
        public Builder deleted(Boolean deleted) { this.deleted = deleted; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public ServiceInstance build() {
            ServiceInstance instance = new ServiceInstance();
            instance.id = this.id;
            instance.serviceId = this.serviceId;
            instance.host = this.host;
            instance.port = this.port;
            instance.weight = this.weight;
            instance.healthy = this.healthy;
            instance.enabled = this.enabled;
            instance.lastHeartbeat = this.lastHeartbeat;
            instance.deleted = this.deleted;
            instance.createdAt = this.createdAt;
            instance.updatedAt = this.updatedAt;
            return instance;
        }
    }
}