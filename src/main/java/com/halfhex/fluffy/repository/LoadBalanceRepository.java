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

public class LoadBalanceRepository {

  private final MySQLPool client;

  public LoadBalanceRepository(MySQLPool client) {
    this.client = client;
  }

  public void save(LoadBalanceStrategy strategy, Promise<Long> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("INSERT INTO load_balance_strategy (name, algorithm, config, created_at, updated_at) VALUES (?, ?, ?, ?, ?)")
        .execute(Tuple.of(
          strategy.getName(),
          strategy.getAlgorithm(),
          strategy.getConfig(),
          strategy.getCreatedAt(),
          strategy.getUpdatedAt()
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

  public void update(LoadBalanceStrategy strategy, Promise<Void> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("UPDATE load_balance_strategy SET name = ?, algorithm = ?, config = ?, updated_at = ? WHERE id = ?")
        .execute(Tuple.of(
          strategy.getName(),
          strategy.getAlgorithm(),
          strategy.getConfig(),
          strategy.getUpdatedAt(),
          strategy.getId()
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
      connection.preparedQuery("UPDATE load_balance_strategy SET deleted = 1, updated_at = ? WHERE id = ?")
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

  public void findById(Long id, Promise<LoadBalanceStrategy> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM load_balance_strategy WHERE id = ? AND deleted = 0")
        .execute(Tuple.of(id), ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          Row row = ar.result().iterator().next();
          promise.complete(row == null ? null : mapRowToStrategy(row));
          connection.close();
        });
    });
  }

  public void findByName(String name, Promise<LoadBalanceStrategy> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM load_balance_strategy WHERE name = ? AND deleted = 0")
        .execute(Tuple.of(name), ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          Row row = ar.result().iterator().next();
          promise.complete(row == null ? null : mapRowToStrategy(row));
          connection.close();
        });
    });
  }

  public void findAll(Promise<List<LoadBalanceStrategy>> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM load_balance_strategy WHERE deleted = 0 ORDER BY created_at DESC")
        .execute(ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          List<LoadBalanceStrategy> strategies = new ArrayList<>();
          for (Row row : ar.result()) {
            strategies.add(mapRowToStrategy(row));
          }
          promise.complete(strategies);
          connection.close();
        });
    });
  }

  private LoadBalanceStrategy mapRowToStrategy(Row row) {
    LoadBalanceStrategy strategy = new LoadBalanceStrategy();
    strategy.setId(row.getLong("id"));
    strategy.setName(row.getString("name"));
    strategy.setAlgorithm(row.getString("algorithm"));
    strategy.setConfig(row.getString("config"));
    strategy.setCreatedAt(row.getLocalDateTime("created_at"));
    strategy.setUpdatedAt(row.getLocalDateTime("updated_at"));
    return strategy;
  }

  public static class LoadBalanceStrategy {
    private Long id;
    private String name;
    private String algorithm;
    private String config;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
  }
}