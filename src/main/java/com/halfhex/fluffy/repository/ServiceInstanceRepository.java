package com.halfhex.fluffy.repository;

import com.halfhex.fluffy.entity.ServiceInstance;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceInstanceRepository {

  private final Pool client;

  public ServiceInstanceRepository(Pool client) {
    this.client = client;
  }

  public void save(ServiceInstance entity, Promise<ServiceInstance> promise) {
    client.withConnection(conn ->
      conn.preparedQuery(
        "INSERT INTO service_instance (service_id, host, port, weight, healthy, enabled, " +
        "last_heartbeat, deleted, created_at, updated_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
      ).execute(Tuple.of(
        entity.getServiceId(),
        entity.getHost(),
        entity.getPort(),
        entity.getWeight(),
        entity.getHealthy(),
        entity.getEnabled(),
        entity.getLastHeartbeat(),
        entity.getDeleted() != null ? entity.getDeleted() : false,
        LocalDateTime.now(),
        LocalDateTime.now()
      )).map(rowSet -> entity)
    ).onComplete(ar -> {
      if (ar.succeeded()) promise.complete(ar.result());
      else promise.fail(ar.cause());
    });
  }

  public void findById(Long id, Promise<ServiceInstance> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("SELECT * FROM service_instance WHERE id = ? AND deleted = 0")
        .execute(Tuple.of(id))
        .map(rowSet -> {
          if (!rowSet.iterator().hasNext()) return null;
          return mapRowToEntity(rowSet.iterator().next());
        })
    ).onComplete(ar -> {
      if (ar.succeeded()) {
        if (ar.result() == null) promise.fail("ServiceInstance not found");
        else promise.complete(ar.result());
      } else promise.fail(ar.cause());
    });
  }

  public void findByServiceId(Long serviceId, Promise<List<ServiceInstance>> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("SELECT * FROM service_instance WHERE service_id = ? AND deleted = 0 ORDER BY weight DESC")
        .execute(Tuple.of(serviceId))
        .map(rowSet -> {
          List<ServiceInstance> list = new ArrayList<>();
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

  public void findAll(Promise<List<ServiceInstance>> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("SELECT * FROM service_instance WHERE deleted = 0 ORDER BY service_id, weight DESC")
        .execute()
        .map(rowSet -> {
          List<ServiceInstance> list = new ArrayList<>();
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

  public void update(ServiceInstance entity, Promise<ServiceInstance> promise) {
    client.withConnection(conn ->
      conn.preparedQuery(
        "UPDATE service_instance SET service_id = ?, host = ?, port = ?, weight = ?, " +
        "healthy = ?, enabled = ?, last_heartbeat = ?, updated_at = ? " +
        "WHERE id = ? AND deleted = 0"
      ).execute(Tuple.of(
        entity.getServiceId(),
        entity.getHost(),
        entity.getPort(),
        entity.getWeight(),
        entity.getHealthy(),
        entity.getEnabled(),
        entity.getLastHeartbeat(),
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
      conn.preparedQuery("UPDATE service_instance SET deleted = 1, updated_at = ? WHERE id = ?")
        .execute(Tuple.of(LocalDateTime.now(), id))
        .mapEmpty()
    ).onComplete(ar -> {
      if (ar.succeeded()) promise.complete();
      else promise.fail(ar.cause());
    });
  }

  private ServiceInstance mapRowToEntity(Row row) {
    if (row == null) return null;
    return ServiceInstance.builder()
      .id(row.getLong("id"))
      .serviceId(row.getLong("service_id"))
      .host(row.getString("host"))
      .port(row.getInteger("port"))
      .weight(row.getInteger("weight"))
      .healthy(row.getBoolean("healthy"))
      .enabled(row.getBoolean("enabled"))
      .lastHeartbeat(row.getLocalDateTime("last_heartbeat"))
      .deleted(row.getBoolean("deleted"))
      .createdAt(row.getLocalDateTime("created_at"))
      .updatedAt(row.getLocalDateTime("updated_at"))
      .build();
  }
}