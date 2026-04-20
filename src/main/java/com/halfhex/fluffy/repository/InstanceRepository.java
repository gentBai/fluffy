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

/**
 * Repository for ServiceInstance entity operations.
 *
 * @author fluffy
 */
public class InstanceRepository {

  private final MySQLPool client;

  public InstanceRepository(MySQLPool client) {
    this.client = client;
  }

  public void save(Instance instance, Promise<Long> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("INSERT INTO service_instance (service_id, host, port, weight, healthy, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)")
        .execute(Tuple.of(
          instance.getServiceId(),
          instance.getHost(),
          instance.getPort(),
          instance.getWeight(),
          instance.isHealthy() ? 0 : 1,
          instance.getCreatedAt(),
          instance.getUpdatedAt()
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

  public void update(Instance instance, Promise<Void> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("UPDATE service_instance SET service_id = ?, host = ?, port = ?, weight = ?, healthy = ?, updated_at = ? WHERE id = ?")
        .execute(Tuple.of(
          instance.getServiceId(),
          instance.getHost(),
          instance.getPort(),
          instance.getWeight(),
          instance.isHealthy() ? 0 : 1,
          instance.getUpdatedAt(),
          instance.getId()
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
      connection.preparedQuery("UPDATE service_instance SET deleted = 1, updated_at = ? WHERE id = ?")
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

  public void findById(Long id, Promise<Instance> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM service_instance WHERE id = ? AND deleted = 0")
        .execute(Tuple.of(id), ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          Row row = ar.result().iterator().next();
          promise.complete(row == null ? null : mapRowToInstance(row));
          connection.close();
        });
    });
  }

  public void findByServiceId(Long serviceId, Promise<List<Instance>> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM service_instance WHERE service_id = ? AND deleted = 0 ORDER BY created_at DESC")
        .execute(Tuple.of(serviceId), ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          List<Instance> instances = new ArrayList<>();
          for (Row row : ar.result()) {
            instances.add(mapRowToInstance(row));
          }
          promise.complete(instances);
          connection.close();
        });
    });
  }

  public void findHealthyByServiceId(Long serviceId, Promise<List<Instance>> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM service_instance WHERE service_id = ? AND healthy = 0 AND deleted = 0 ORDER BY created_at DESC")
        .execute(Tuple.of(serviceId), ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          List<Instance> instances = new ArrayList<>();
          for (Row row : ar.result()) {
            instances.add(mapRowToInstance(row));
          }
          promise.complete(instances);
          connection.close();
        });
    });
  }

  public void findAll(Promise<List<Instance>> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM service_instance WHERE deleted = 0 ORDER BY created_at DESC")
        .execute(ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          List<Instance> instances = new ArrayList<>();
          for (Row row : ar.result()) {
            instances.add(mapRowToInstance(row));
          }
          promise.complete(instances);
          connection.close();
        });
    });
  }

  private Instance mapRowToInstance(Row row) {
    Instance instance = new Instance();
    instance.setId(row.getLong("id"));
    instance.setServiceId(row.getLong("service_id"));
    instance.setHost(row.getString("host"));
    instance.setPort(row.getInteger("port"));
    instance.setWeight(row.getInteger("weight"));
    instance.setHealthy(row.getInteger("healthy") == 0);
    instance.setCreatedAt(row.getLocalDateTime("created_at"));
    instance.setUpdatedAt(row.getLocalDateTime("updated_at"));
    return instance;
  }

  public static class Instance {
    private Long id;
    private Long serviceId;
    private String host;
    private Integer port;
    private Integer weight;
    private boolean healthy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
    public boolean isHealthy() { return healthy; }
    public void setHealthy(boolean healthy) { this.healthy = healthy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
  }
}