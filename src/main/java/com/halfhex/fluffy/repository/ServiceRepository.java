package com.halfhex.fluffy.repository;

import com.halfhex.fluffy.entity.GatewayService;
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

public class ServiceRepository {

  private final MySQLPool client;

  public ServiceRepository(MySQLPool client) {
    this.client = client;
  }

  public void save(GatewayService service, Promise<Long> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      String sql = "INSERT INTO gateway_service (name, base_url, health_check_url, " +
                   "health_check_interval, max_connections, timeout_ms, deleted, created_at, updated_at) " +
                   "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
      connection.preparedQuery(sql)
        .execute(Tuple.of(
          service.getName(),
          service.getBaseUrl(),
          service.getHealthCheckUrl(),
          service.getHealthCheckInterval(),
          service.getMaxConnections(),
          service.getTimeoutMs(),
          service.getDeleted() ? 1 : 0,
          service.getCreatedAt(),
          service.getUpdatedAt()
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

  public void update(GatewayService service, Promise<Void> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      String sql = "UPDATE gateway_service SET name = ?, base_url = ?, health_check_url = ?, " +
                   "health_check_interval = ?, max_connections = ?, timeout_ms = ?, " +
                   "deleted = ?, updated_at = ? WHERE id = ?";
      connection.preparedQuery(sql)
        .execute(Tuple.of(
          service.getName(),
          service.getBaseUrl(),
          service.getHealthCheckUrl(),
          service.getHealthCheckInterval(),
          service.getMaxConnections(),
          service.getTimeoutMs(),
          service.getDeleted() ? 1 : 0,
          LocalDateTime.now(),
          service.getId()
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

  public void softDelete(Long id, Promise<Void> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("UPDATE gateway_service SET deleted = 1, updated_at = ? WHERE id = ?")
        .execute(Tuple.of(LocalDateTime.now(), id), ar -> {
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

  public void findById(Long id, Promise<GatewayService> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM gateway_service WHERE id = ? AND deleted = 0")
        .execute(Tuple.of(id), ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          Row row = ar.result().iterator().next();
          promise.complete(row == null ? null : mapRowToEntity(row));
          connection.close();
        });
    });
  }

  public void findByName(String name, Promise<GatewayService> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM gateway_service WHERE name = ? AND deleted = 0")
        .execute(Tuple.of(name), ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          Row row = ar.result().iterator().next();
          promise.complete(row == null ? null : mapRowToEntity(row));
          connection.close();
        });
    });
  }

  public void findAllEnabled(Promise<List<GatewayService>> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM gateway_service WHERE deleted = 0 ORDER BY created_at DESC")
        .execute(ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          List<GatewayService> services = new ArrayList<>();
          for (Row row : ar.result()) {
            services.add(mapRowToEntity(row));
          }
          promise.complete(services);
          connection.close();
        });
    });
  }

  private GatewayService mapRowToEntity(Row row) {
    return GatewayService.builder()
      .id(row.getLong("id"))
      .name(row.getString("name"))
      .baseUrl(row.getString("base_url"))
      .healthCheckUrl(row.getString("health_check_url"))
      .healthCheckInterval(row.getInteger("health_check_interval"))
      .maxConnections(row.getInteger("max_connections"))
      .timeoutMs(row.getInteger("timeout_ms"))
      .deleted(row.getInteger("deleted") == 1)
      .createdAt(row.getLocalDateTime("created_at"))
      .updatedAt(row.getLocalDateTime("updated_at"))
      .build();
  }
}
