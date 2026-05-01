package com.halfhex.fluffy.repository;

import com.halfhex.fluffy.entity.GatewayService;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GatewayServiceRepository {

  private final Pool client;

  public GatewayServiceRepository(Pool client) {
    this.client = client;
  }

  public void save(GatewayService entity, Promise<GatewayService> promise) {
    client.withConnection(conn ->
      conn.preparedQuery(
        "INSERT INTO gateway_service (name, base_url, health_check_url, health_check_interval, " +
        "max_connections, timeout_ms, deleted, created_at, updated_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
      ).execute(Tuple.of(
        entity.getName(),
        entity.getBaseUrl(),
        entity.getHealthCheckUrl(),
        entity.getHealthCheckInterval(),
        entity.getMaxConnections(),
        entity.getTimeoutMs(),
        entity.getDeleted() != null ? entity.getDeleted() : false,
        LocalDateTime.now(),
        LocalDateTime.now()
      )).map(rowSet -> entity)
    ).onComplete(ar -> {
      if (ar.succeeded()) promise.complete(ar.result());
      else promise.fail(ar.cause());
    });
  }

  public void findById(Long id, Promise<GatewayService> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("SELECT * FROM gateway_service WHERE id = ? AND deleted = 0")
        .execute(Tuple.of(id))
        .map(rowSet -> {
          if (!rowSet.iterator().hasNext()) return null;
          return mapRowToEntity(rowSet.iterator().next());
        })
    ).onComplete(ar -> {
      if (ar.succeeded()) {
        if (ar.result() == null) promise.fail("GatewayService not found");
        else promise.complete(ar.result());
      } else promise.fail(ar.cause());
    });
  }

  public void findAll(Promise<List<GatewayService>> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("SELECT * FROM gateway_service WHERE deleted = 0 ORDER BY name")
        .execute()
        .map(rowSet -> {
          List<GatewayService> list = new ArrayList<>();
          for (Row row : rowSet) {
            list.add(mapRowToEntity(row));
          }
          return list;
        })
    ).onComplete(ar -> {
      if (ar.succeeded()) promise.complete(ar.result());
      else promise.fail(ar.cause());
    });
  }

  public void update(GatewayService entity, Promise<GatewayService> promise) {
    client.withConnection(conn ->
      conn.preparedQuery(
        "UPDATE gateway_service SET name = ?, base_url = ?, health_check_url = ?, " +
        "health_check_interval = ?, max_connections = ?, timeout_ms = ?, updated_at = ? " +
        "WHERE id = ? AND deleted = 0"
      ).execute(Tuple.of(
        entity.getName(),
        entity.getBaseUrl(),
        entity.getHealthCheckUrl(),
        entity.getHealthCheckInterval(),
        entity.getMaxConnections(),
        entity.getTimeoutMs(),
        LocalDateTime.now(),
        entity.getId()
      )).map(rowSet -> entity)
    ).onComplete(ar -> {
      if (ar.succeeded()) promise.complete(ar.result());
      else promise.fail(ar.cause());
    });
  }

  public void softDelete(Long id, Promise<Void> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("UPDATE gateway_service SET deleted = 1, updated_at = ? WHERE id = ?")
        .execute(Tuple.of(LocalDateTime.now(), id))
        .mapEmpty()
    ).onComplete(ar -> {
      if (ar.succeeded()) promise.complete();
      else promise.fail(ar.cause());
    });
  }

  private GatewayService mapRowToEntity(Row row) {
    if (row == null) return null;
    return GatewayService.builder()
      .id(row.getLong("id"))
      .name(row.getString("name"))
      .baseUrl(row.getString("base_url"))
      .healthCheckUrl(row.getString("health_check_url"))
      .healthCheckInterval(row.getInteger("health_check_interval"))
      .maxConnections(row.getInteger("max_connections"))
      .timeoutMs(row.getInteger("timeout_ms"))
      .deleted(row.getBoolean("deleted"))
      .createdAt(row.getLocalDateTime("created_at"))
      .updatedAt(row.getLocalDateTime("updated_at"))
      .build();
  }
}