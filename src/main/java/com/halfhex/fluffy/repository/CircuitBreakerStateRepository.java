package com.halfhex.fluffy.repository;

import com.halfhex.fluffy.entity.CircuitBreakerState;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CircuitBreakerStateRepository {

  private final Pool client;

  public CircuitBreakerStateRepository(Pool client) {
    this.client = client;
  }

  public void save(CircuitBreakerState entity, Promise<CircuitBreakerState> promise) {
    client.withConnection(conn ->
      conn.preparedQuery(
        "INSERT INTO circuit_breaker_state (service_id, instance_id, state, failure_count, " +
        "success_count, last_failure_time, last_state_change, updated_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
      ).execute(Tuple.of(
        entity.getServiceId(),
        entity.getInstanceId(),
        entity.getState() != null ? entity.getState() : "CLOSED",
        entity.getFailureCount() != null ? entity.getFailureCount() : 0,
        entity.getSuccessCount() != null ? entity.getSuccessCount() : 0,
        entity.getLastFailureTime(),
        entity.getLastStateChange() != null ? entity.getLastStateChange() : LocalDateTime.now(),
        LocalDateTime.now()
      )).map(rowSet -> entity)
    ).onComplete(ar -> {
      if (ar.succeeded()) promise.complete(ar.result());
      else promise.fail(ar.cause());
    });
  }

  public void findById(Long id, Promise<CircuitBreakerState> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("SELECT * FROM circuit_breaker_state WHERE id = ?")
        .execute(Tuple.of(id))
        .map(rowSet -> {
          if (!rowSet.iterator().hasNext()) return null;
          return mapRowToEntity(rowSet.iterator().next());
        })
    ).onComplete(ar -> {
      if (ar.succeeded()) {
        if (ar.result() == null) promise.fail("CircuitBreakerState not found");
        else promise.complete(ar.result());
      } else promise.fail(ar.cause());
    });
  }

  public void findByServiceId(Long serviceId, Promise<CircuitBreakerState> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("SELECT * FROM circuit_breaker_state WHERE service_id = ?")
        .execute(Tuple.of(serviceId))
        .map(rowSet -> {
          if (!rowSet.iterator().hasNext()) return null;
          return mapRowToEntity(rowSet.iterator().next());
        })
    ).onComplete(ar -> {
      if (ar.succeeded()) {
        if (ar.result() == null) promise.fail("CircuitBreakerState not found for service");
        else promise.complete(ar.result());
      } else promise.fail(ar.cause());
    });
  }

  public void findByInstanceId(Long instanceId, Promise<CircuitBreakerState> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("SELECT * FROM circuit_breaker_state WHERE instance_id = ?")
        .execute(Tuple.of(instanceId))
        .map(rowSet -> {
          if (!rowSet.iterator().hasNext()) return null;
          return mapRowToEntity(rowSet.iterator().next());
        })
    ).onComplete(ar -> {
      if (ar.succeeded()) {
        if (ar.result() == null) promise.fail("CircuitBreakerState not found for instance");
        else promise.complete(ar.result());
      } else promise.fail(ar.cause());
    });
  }

  public void findAll(Promise<List<CircuitBreakerState>> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("SELECT * FROM circuit_breaker_state ORDER BY service_id")
        .execute()
        .map(rowSet -> {
          List<CircuitBreakerState> list = new ArrayList<>();
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

  public void update(CircuitBreakerState entity, Promise<CircuitBreakerState> promise) {
    client.withConnection(conn ->
      conn.preparedQuery(
        "UPDATE circuit_breaker_state SET service_id = ?, instance_id = ?, state = ?, " +
        "failure_count = ?, success_count = ?, last_failure_time = ?, last_state_change = ?, " +
        "updated_at = ? WHERE id = ?"
      ).execute(Tuple.of(
        entity.getServiceId(),
        entity.getInstanceId(),
        entity.getState(),
        entity.getFailureCount(),
        entity.getSuccessCount(),
        entity.getLastFailureTime(),
        entity.getLastStateChange(),
        LocalDateTime.now(),
        entity.getId()
      )).map(rowSet -> entity)
    ).onComplete(ar -> {
      if (ar.succeeded()) promise.complete(ar.result());
      else promise.fail(ar.cause());
    });
  }

  public void delete(Long id, Promise<Void> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("DELETE FROM circuit_breaker_state WHERE id = ?")
        .execute(Tuple.of(id))
        .mapEmpty()
    ).onComplete(ar -> {
      if (ar.succeeded()) promise.complete();
      else promise.fail(ar.cause());
    });
  }

  private CircuitBreakerState mapRowToEntity(Row row) {
    if (row == null) return null;
    CircuitBreakerState state = new CircuitBreakerState();
    state.setId(row.getLong("id"));
    state.setServiceId(row.getLong("service_id"));
    state.setInstanceId(row.getLong("instance_id"));
    state.setState(row.getString("state"));
    state.setFailureCount(row.getInteger("failure_count"));
    state.setSuccessCount(row.getInteger("success_count"));
    state.setLastFailureTime(row.getLocalDateTime("last_failure_time"));
    state.setLastStateChange(row.getLocalDateTime("last_state_change"));
    state.setUpdatedAt(row.getLocalDateTime("updated_at"));
    return state;
  }
}
