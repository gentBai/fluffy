package com.halfhex.fluffy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("circuit_breaker_config")
public class CircuitBreakerState {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("service_id")
    private Long serviceId;

    @TableField("instance_id")
    private Long instanceId;

    @TableField("enabled")
    private Boolean enabled;

    @TableField("failure_threshold")
    private Integer failureThreshold;

    @TableField("success_threshold")
    private Integer successThreshold;

    @TableField("timeout_seconds")
    private Integer timeoutSeconds;

    @TableField("reset_seconds")
    private Integer resetSeconds;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    public CircuitBreakerState() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public Long getInstanceId() { return instanceId; }
    public void setInstanceId(Long instanceId) { this.instanceId = instanceId; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Integer getFailureThreshold() { return failureThreshold; }
    public void setFailureThreshold(Integer failureThreshold) { this.failureThreshold = failureThreshold; }
    public Integer getSuccessThreshold() { return successThreshold; }
    public void setSuccessThreshold(Integer successThreshold) { this.successThreshold = successThreshold; }
    public Integer getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(Integer timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    public Integer getResetSeconds() { return resetSeconds; }
    public void setResetSeconds(Integer resetSeconds) { this.resetSeconds = resetSeconds; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}