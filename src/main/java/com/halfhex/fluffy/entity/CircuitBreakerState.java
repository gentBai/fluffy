package com.halfhex.fluffy.entity;

import java.time.LocalDateTime;

public class CircuitBreakerState {

    private Long id;

    private Long serviceId;

    private Long instanceId;

    private String state;

    private Integer failureCount;

    private Integer successCount;

    private LocalDateTime lastFailureTime;

    private LocalDateTime lastStateChange;

    private LocalDateTime updatedAt;

    public CircuitBreakerState() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public Long getInstanceId() { return instanceId; }
    public void setInstanceId(Long instanceId) { this.instanceId = instanceId; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Integer getFailureCount() { return failureCount; }
    public void setFailureCount(Integer failureCount) { this.failureCount = failureCount; }
    public Integer getSuccessCount() { return successCount; }
    public void setSuccessCount(Integer successCount) { this.successCount = successCount; }
    public LocalDateTime getLastFailureTime() { return lastFailureTime; }
    public void setLastFailureTime(LocalDateTime lastFailureTime) { this.lastFailureTime = lastFailureTime; }
    public LocalDateTime getLastStateChange() { return lastStateChange; }
    public void setLastStateChange(LocalDateTime lastStateChange) { this.lastStateChange = lastStateChange; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
