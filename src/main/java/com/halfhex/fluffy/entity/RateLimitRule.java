package com.halfhex.fluffy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("rate_limit_rule")
public class RateLimitRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("route_id")
    private Long routeId;

    @TableField("service_id")
    private Long serviceId;

    @TableField("limit_type")
    private String limitType;

    @TableField("max_requests")
    private Integer maxRequests;

    @TableField("requests_per_minute")
    private Integer requestsPerMinute;

    @TableField("requests_per_hour")
    private Integer requestsPerHour;

    @TableField("requests_per_day")
    private Integer requestsPerDay;

    @TableField("burst_size")
    private Integer burstSize;

    @TableField("deleted")
    private Boolean deleted;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
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