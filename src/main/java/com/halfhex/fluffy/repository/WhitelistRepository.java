package com.halfhex.fluffy.repository;

import io.vertx.core.Promise;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WhitelistRepository {

  private final MySQLPool client;

  public WhitelistRepository(MySQLPool client) {
    this.client = client;
  }

  public void save(WhitelistEntry entry, Promise<Long> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("INSERT INTO whitelist (target_type, target_value, description, created_at) VALUES (?, ?, ?, ?)")
        .execute(Tuple.of(
          entry.getTargetType(),
          entry.getTargetValue(),
          entry.getDescription(),
          entry.getCreatedAt()
        ), ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          Long id = ar.result().property(MySQLClient.LAST_INSERTED_ID);
          promise.complete(id);
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
      connection.preparedQuery("UPDATE whitelist SET deleted = 1, updated_at = ? WHERE id = ?")
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

  public void findById(Long id, Promise<WhitelistEntry> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM whitelist WHERE id = ? AND deleted = 0")
        .execute(Tuple.of(id), ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          Row row = ar.result().iterator().next();
          promise.complete(row == null ? null : mapRowToEntry(row));
          connection.close();
        });
    });
  }

  public void checkIfWhitelisted(String targetType, String targetValue, Promise<Boolean> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM whitelist WHERE target_type = ? AND target_value = ? AND deleted = 0")
        .execute(Tuple.of(targetType, targetValue), ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          Row row = ar.result().iterator().next();
          promise.complete(row != null);
          connection.close();
        });
    });
  }

  public void findAll(Promise<List<WhitelistEntry>> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM whitelist WHERE deleted = 0 ORDER BY created_at DESC")
        .execute(ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          List<WhitelistEntry> entries = new ArrayList<>();
          for (Row row : ar.result()) {
            entries.add(mapRowToEntry(row));
          }
          promise.complete(entries);
          connection.close();
        });
    });
  }

  private WhitelistEntry mapRowToEntry(Row row) {
    WhitelistEntry entry = new WhitelistEntry();
    entry.setId(row.getLong("id"));
    entry.setTargetType(row.getString("target_type"));
    entry.setTargetValue(row.getString("target_value"));
    entry.setDescription(row.getString("description"));
    entry.setCreatedAt(row.getLocalDateTime("created_at"));
    return entry;
  }

  public static class WhitelistEntry {
    private Long id;
    private String targetType;
    private String targetValue;
    private String description;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetValue() { return targetValue; }
    public void setTargetValue(String targetValue) { this.targetValue = targetValue; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
  }
}