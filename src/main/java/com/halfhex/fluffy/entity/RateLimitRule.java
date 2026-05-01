package com.halfhex.fluffy.entity;

import java.time.LocalDateTime;

public class RateLimitRule {

    private Long id;

    private String name;

    private Long routeId;

    private Long serviceId;

    private String limitType;

    private Integer maxRequests;

    private Integer requestsPerMinute;

    private Integer requestsPerHour;

    private Integer requestsPerDay;

    private Integer burstSize;

    private Boolean deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public RateLimitRule() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public String getLimitType() { return limitType; }
    public void setLimitType(String limitType) { this.limitType = limitType; }
    public Integer getMaxRequests() { return maxRequests; }
    public void setMaxRequests(Integer maxRequests) { this.maxRequests = maxRequests; }
    public Integer getRequestsPerMinute() { return requestsPerMinute; }
    public void setRequestsPerMinute(Integer requestsPerMinute) { this.requestsPerMinute = requestsPerMinute; }
    public Integer getRequestsPerHour() { return requestsPerHour; }
    public void setRequestsPerHour(Integer requestsPerHour) { this.requestsPerHour = requestsPerHour; }
    public Integer getRequestsPerDay() { return requestsPerDay; }
    public void setRequestsPerDay(Integer requestsPerDay) { this.requestsPerDay = requestsPerDay; }
    public Integer getBurstSize() { return burstSize; }
    public void setBurstSize(Integer burstSize) { this.burstSize = burstSize; }
    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}