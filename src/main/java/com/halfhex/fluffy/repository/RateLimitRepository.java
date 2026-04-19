package com.halfhex.fluffy.repository;

import io.vertx.core.Promise;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RateLimitRepository {

  private final MySQLPool client;

  public RateLimitRepository(MySQLPool client) {
    this.client = client;
  }

  public void save(RateLimitRule rule, Promise<Long> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("INSERT INTO rate_limit_rule (name, route_id, service_id, limit_type, max_requests, window_seconds, deleted, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")
        .execute(Tuple.of(
          rule.getName(),
          rule.getRouteId(),
          rule.getServiceId(),
          rule.getLimitType(),
          rule.getMaxRequests(),
          rule.getWindowSeconds(),
          rule.isEnabled() ? 0 : 1,
          rule.getCreatedAt(),
          rule.getUpdatedAt()
        ), ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          RowSet<Row> rows = ar.result();
          Long id = rows.property(MySQLClient.LAST_INSERTED_ID);
          promise.complete(id);
          connection.close();
        });
    });
  }

  public void update(RateLimitRule rule, Promise<Void> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("UPDATE rate_limit_rule SET name = ?, route_id = ?, service_id = ?, limit_type = ?, max_requests = ?, window_seconds = ?, deleted = ?, updated_at = ? WHERE id = ?")
        .execute(Tuple.of(
          rule.getName(),
          rule.getRouteId(),
          rule.getServiceId(),
          rule.getLimitType(),
          rule.getMaxRequests(),
          rule.getWindowSeconds(),
          rule.isEnabled() ? 0 : 1,
          rule.getUpdatedAt(),
          rule.getId()
        ), ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          promise.complete();
          connection.close();
        });
    });
  }

  public void delete(Long id, Promise<Void> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      LocalDateTime now = LocalDateTime.now();
      connection.preparedQuery("UPDATE rate_limit_rule SET deleted = 1, updated_at = ? WHERE id = ?")
        .execute(Tuple.of(now, id), ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          promise.complete();
          connection.close();
        });
    });
  }

  public void findById(Long id, Promise<RateLimitRule> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM rate_limit_rule WHERE id = ? AND deleted = 0")
        .execute(Tuple.of(id), ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          Row row = ar.result().iterator().next();
          promise.complete(row == null ? null : mapRowToRule(row));
          connection.close();
        });
    });
  }

  public void findByRouteId(Long routeId, Promise<List<RateLimitRule>> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM rate_limit_rule WHERE route_id = ? AND deleted = 0 ORDER BY created_at DESC")
        .execute(Tuple.of(routeId), ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          List<RateLimitRule> rules = new ArrayList<>();
          for (Row row : ar.result()) {
            rules.add(mapRowToRule(row));
          }
          promise.complete(rules);
          connection.close();
        });
    });
  }

  public void findByServiceId(Long serviceId, Promise<List<RateLimitRule>> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM rate_limit_rule WHERE service_id = ? AND deleted = 0 ORDER BY created_at DESC")
        .execute(Tuple.of(serviceId), ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          List<RateLimitRule> rules = new ArrayList<>();
          for (Row row : ar.result()) {
            rules.add(mapRowToRule(row));
          }
          promise.complete(rules);
          connection.close();
        });
    });
  }

  public void findAllEnabled(Promise<List<RateLimitRule>> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM rate_limit_rule WHERE deleted = 0 ORDER BY created_at DESC")
        .execute(ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          List<RateLimitRule> rules = new ArrayList<>();
          for (Row row : ar.result()) {
            rules.add(mapRowToRule(row));
          }
          promise.complete(rules);
          connection.close();
        });
    });
  }

  public void findAll(Promise<List<RateLimitRule>> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM rate_limit_rule WHERE deleted = 0 ORDER BY created_at DESC")
        .execute(ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          List<RateLimitRule> rules = new ArrayList<>();
          for (Row row : ar.result()) {
            rules.add(mapRowToRule(row));
          }
          promise.complete(rules);
          connection.close();
        });
    });
  }

  private RateLimitRule mapRowToRule(Row row) {
    RateLimitRule rule = new RateLimitRule();
    rule.setId(row.getLong("id"));
    rule.setName(row.getString("name"));
    rule.setRouteId(row.getLong("route_id"));
    rule.setServiceId(row.getLong("service_id"));
    rule.setLimitType(row.getString("limit_type"));
    rule.setMaxRequests(row.getLong("max_requests"));
    rule.setWindowSeconds(row.getInteger("window_seconds"));
    rule.setEnabled(row.getInteger("deleted") == 0);
    rule.setCreatedAt(row.getLocalDateTime("created_at"));
    rule.setUpdatedAt(row.getLocalDateTime("updated_at"));
    return rule;
  }

  public static class RateLimitRule {
    private Long id;
    private String name;
    private Long routeId;
    private Long serviceId;
    private String limitType;
    private Long maxRequests;
    private Integer windowSeconds;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
    public Long getMaxRequests() { return maxRequests; }
    public void setMaxRequests(Long maxRequests) { this.maxRequests = maxRequests; }
    public Integer getWindowSeconds() { return windowSeconds; }
    public void setWindowSeconds(Integer windowSeconds) { this.windowSeconds = windowSeconds; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
  }
}