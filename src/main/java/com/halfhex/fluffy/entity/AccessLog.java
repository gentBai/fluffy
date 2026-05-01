package com.halfhex.fluffy.entity;

import java.time.LocalDateTime;

public class AccessLog {

  private Long id;
  private String traceId;
  private String requestMethod;
  private String requestPath;
  private String requestQuery;
  private String requestBody;
  private Integer responseStatus;
  private Integer responseTimeMs;
  private String clientIp;
  private Long userId;
  private Long apiKeyId;
  private Long routeId;
  private Long serviceId;
  private Long instanceId;
  private String errorMessage;
  private LocalDateTime createdAt;

  public AccessLog() {
  }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getTraceId() { return traceId; }
  public void setTraceId(String traceId) { this.traceId = traceId; }
  public String getRequestMethod() { return requestMethod; }
  public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }
  public String getRequestPath() { return requestPath; }
  public void setRequestPath(String requestPath) { this.requestPath = requestPath; }
  public String getRequestQuery() { return requestQuery; }
  public void setRequestQuery(String requestQuery) { this.requestQuery = requestQuery; }
  public String getRequestBody() { return requestBody; }
  public void setRequestBody(String requestBody) { this.requestBody = requestBody; }
  public Integer getResponseStatus() { return responseStatus; }
  public void setResponseStatus(Integer responseStatus) { this.responseStatus = responseStatus; }
  public Integer getResponseTimeMs() { return responseTimeMs; }
  public void setResponseTimeMs(Integer responseTimeMs) { this.responseTimeMs = responseTimeMs; }
  public String getClientIp() { return clientIp; }
  public void setClientIp(String clientIp) { this.clientIp = clientIp; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public Long getApiKeyId() { return apiKeyId; }
  public void setApiKeyId(Long apiKeyId) { this.apiKeyId = apiKeyId; }
  public Long getRouteId() { return routeId; }
  public void setRouteId(Long routeId) { this.routeId = routeId; }
  public Long getServiceId() { return serviceId; }
  public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
  public Long getInstanceId() { return instanceId; }
  public void setInstanceId(Long instanceId) { this.instanceId = instanceId; }
  public String getErrorMessage() { return errorMessage; }
  public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
