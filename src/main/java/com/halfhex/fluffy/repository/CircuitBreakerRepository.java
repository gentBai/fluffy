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

public class CircuitBreakerRepository {

  private final MySQLPool client;

  public CircuitBreakerRepository(MySQLPool client) {
    this.client = client;
  }

  public void save(CircuitBreakerConfig config, Promise<Long> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("INSERT INTO circuit_breaker_config (service_id, instance_id, enabled, failure_threshold, success_threshold, timeout_seconds, reset_seconds, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")
        .execute(Tuple.of(
          config.getServiceId(),
          config.getInstanceId(),
          config.isEnabled() ? 0 : 1,
          config.getFailureThreshold(),
          config.getSuccessThreshold(),
          config.getTimeoutSeconds(),
          config.getResetSeconds(),
          config.getCreatedAt(),
          config.getUpdatedAt()
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

  public void update(CircuitBreakerConfig config, Promise<Void> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("UPDATE circuit_breaker_config SET service_id = ?, instance_id = ?, enabled = ?, failure_threshold = ?, success_threshold = ?, timeout_seconds = ?, reset_seconds = ?, updated_at = ? WHERE id = ?")
        .execute(Tuple.of(
          config.getServiceId(),
          config.getInstanceId(),
          config.isEnabled() ? 0 : 1,
          config.getFailureThreshold(),
          config.getSuccessThreshold(),
          config.getTimeoutSeconds(),
          config.getResetSeconds(),
          config.getUpdatedAt(),
          config.getId()
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
      connection.preparedQuery("UPDATE circuit_breaker_config SET deleted = 1, updated_at = ? WHERE id = ?")
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

  public void findById(Long id, Promise<CircuitBreakerConfig> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM circuit_breaker_config WHERE id = ? AND deleted = 0")
        .execute(Tuple.of(id), ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          Row row = ar.result().iterator().next();
          promise.complete(row == null ? null : mapRowToConfig(row));
          connection.close();
        });
    });
  }

  public void findByServiceAndInstance(Long serviceId, Long instanceId, Promise<CircuitBreakerConfig> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM circuit_breaker_config WHERE service_id = ? AND instance_id = ? AND deleted = 0")
        .execute(Tuple.of(serviceId, instanceId), ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          Row row = ar.result().iterator().next();
          promise.complete(row == null ? null : mapRowToConfig(row));
          connection.close();
        });
    });
  }

  public void findByServiceId(Long serviceId, Promise<List<CircuitBreakerConfig>> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM circuit_breaker_config WHERE service_id = ? AND deleted = 0 ORDER BY created_at DESC")
        .execute(Tuple.of(serviceId), ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          List<CircuitBreakerConfig> configs = new ArrayList<>();
          for (Row row : ar.result()) {
            configs.add(mapRowToConfig(row));
          }
          promise.complete(configs);
          connection.close();
        });
    });
  }

  public void findAll(Promise<List<CircuitBreakerConfig>> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM circuit_breaker_config WHERE deleted = 0 ORDER BY created_at DESC")
        .execute(ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          List<CircuitBreakerConfig> configs = new ArrayList<>();
          for (Row row : ar.result()) {
            configs.add(mapRowToConfig(row));
          }
          promise.complete(configs);
          connection.close();
        });
    });
  }

  private CircuitBreakerConfig mapRowToConfig(Row row) {
    CircuitBreakerConfig config = new CircuitBreakerConfig();
    config.setId(row.getLong("id"));
    config.setServiceId(row.getLong("service_id"));
    config.setInstanceId(row.getLong("instance_id"));
    config.setEnabled(row.getInteger("enabled") == 0);
    config.setFailureThreshold(row.getInteger("failure_threshold"));
    config.setSuccessThreshold(row.getInteger("success_threshold"));
    config.setTimeoutSeconds(row.getInteger("timeout_seconds"));
    config.setResetSeconds(row.getInteger("reset_seconds"));
    config.setCreatedAt(row.getLocalDateTime("created_at"));
    config.setUpdatedAt(row.getLocalDateTime("updated_at"));
    return config;
  }

  public static class CircuitBreakerConfig {
    private Long id;
    private Long serviceId;
    private Long instanceId;
    private boolean enabled;
    private Integer failureThreshold;
    private Integer successThreshold;
    private Integer timeoutSeconds;
    private Integer resetSeconds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public Long getInstanceId() { return instanceId; }
    public void setInstanceId(Long instanceId) { this.instanceId = instanceId; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
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
}