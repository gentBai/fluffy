package com.halfhex.fluffy.repository;

import com.halfhex.fluffy.entity.LoadBalanceStrategy;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LoadBalanceStrategyRepository {

  private final Pool client;

  public LoadBalanceStrategyRepository(Pool client) {
    this.client = client;
  }

  public void save(LoadBalanceStrategy entity, Promise<LoadBalanceStrategy> promise) {
    client.withConnection(conn ->
      conn.preparedQuery(
        "INSERT INTO load_balance_strategy (service_id, name, algorithm, config, enabled, created_at, updated_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)"
      ).execute(Tuple.of(
        entity.getServiceId(),
        entity.getName(),
        entity.getAlgorithm(),
        entity.getConfig(),
        entity.getEnabled() != null ? entity.getEnabled() : true,
        LocalDateTime.now(),
        LocalDateTime.now()
      )).map(rowSet -> entity)
    ).onComplete(ar -> {
      if (ar.succeeded()) promise.complete(ar.result());
      else promise.fail(ar.cause());
    });
  }

  public void findById(Long id, Promise<LoadBalanceStrategy> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("SELECT * FROM load_balance_strategy WHERE id = ?")
        .execute(Tuple.of(id))
        .map(rowSet -> {
          if (!rowSet.iterator().hasNext()) return null;
          return mapRowToEntity(rowSet.iterator().next());
        })
    ).onComplete(ar -> {
      if (ar.succeeded()) {
        if (ar.result() == null) promise.fail("LoadBalanceStrategy not found");
        else promise.complete(ar.result());
      } else promise.fail(ar.cause());
    });
  }

  public void findByServiceId(Long serviceId, Promise<LoadBalanceStrategy> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("SELECT * FROM load_balance_strategy WHERE service_id = ? AND enabled = true")
        .execute(Tuple.of(serviceId))
        .map(rowSet -> {
          if (!rowSet.iterator().hasNext()) return null;
          return mapRowToEntity(rowSet.iterator().next());
        })
    ).onComplete(ar -> {
      if (ar.succeeded()) {
        if (ar.result() == null) promise.fail("LoadBalanceStrategy not found for service");
        else promise.complete(ar.result());
      } else promise.fail(ar.cause());
    });
  }

  public void findAll(Promise<List<LoadBalanceStrategy>> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("SELECT * FROM load_balance_strategy ORDER BY service_id, name")
        .execute()
        .map(rowSet -> {
          List<LoadBalanceStrategy> list = new ArrayList<>();
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

  public void update(LoadBalanceStrategy entity, Promise<LoadBalanceStrategy> promise) {
    client.withConnection(conn ->
      conn.preparedQuery(
        "UPDATE load_balance_strategy SET service_id = ?, name = ?, algorithm = ?, " +
        "config = ?, enabled = ?, updated_at = ? WHERE id = ?"
      ).execute(Tuple.of(
        entity.getServiceId(),
        entity.getName(),
        entity.getAlgorithm(),
        entity.getConfig(),
        entity.getEnabled(),
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
      conn.preparedQuery("DELETE FROM load_balance_strategy WHERE id = ?")
        .execute(Tuple.of(id))
        .mapEmpty()
    ).onComplete(ar -> {
      if (ar.succeeded()) promise.complete();
      else promise.fail(ar.cause());
    });
  }

  private LoadBalanceStrategy mapRowToEntity(Row row) {
    if (row == null) return null;
    return new LoadBalanceStrategy() {{
      setId(row.getLong("id"));
      setServiceId(row.getLong("service_id"));
      setName(row.getString("name"));
      setAlgorithm(row.getString("algorithm"));
      setConfig(row.getString("config"));
      setEnabled(row.getBoolean("enabled"));
      setCreatedAt(row.getLocalDateTime("created_at"));
      setUpdatedAt(row.getLocalDateTime("updated_at"));
    }};
  }
}