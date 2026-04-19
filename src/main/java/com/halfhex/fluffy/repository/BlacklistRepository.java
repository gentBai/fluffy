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

public class BlacklistRepository {

  private final MySQLPool client;

  public BlacklistRepository(MySQLPool client) {
    this.client = client;
  }

  public void save(BlacklistEntry entry, Promise<Long> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("INSERT INTO blacklist (target_type, target_value, reason, expires_at, created_at) VALUES (?, ?, ?, ?, ?)")
        .execute(Tuple.of(
          entry.getTargetType(),
          entry.getTargetValue(),
          entry.getReason(),
          entry.getExpiresAt(),
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
      connection.preparedQuery("UPDATE blacklist SET deleted = 1, updated_at = ? WHERE id = ?")
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

  public void findById(Long id, Promise<BlacklistEntry> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM blacklist WHERE id = ? AND deleted = 0")
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

  public void checkIfBlacklisted(String targetType, String targetValue, Promise<Boolean> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      LocalDateTime now = LocalDateTime.now();
      connection.preparedQuery("SELECT * FROM blacklist WHERE target_type = ? AND target_value = ? AND deleted = 0 AND (expires_at IS NULL OR expires_at > ?)")
        .execute(Tuple.of(targetType, targetValue, now), ar -> {
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

  public void findAll(Promise<List<BlacklistEntry>> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM blacklist WHERE deleted = 0 ORDER BY created_at DESC")
        .execute(ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          List<BlacklistEntry> entries = new ArrayList<>();
          for (Row row : ar.result()) {
            entries.add(mapRowToEntry(row));
          }
          promise.complete(entries);
          connection.close();
        });
    });
  }

  private BlacklistEntry mapRowToEntry(Row row) {
    BlacklistEntry entry = new BlacklistEntry();
    entry.setId(row.getLong("id"));
    entry.setTargetType(row.getString("target_type"));
    entry.setTargetValue(row.getString("target_value"));
    entry.setReason(row.getString("reason"));
    entry.setExpiresAt(row.getLocalDateTime("expires_at"));
    entry.setCreatedAt(row.getLocalDateTime("created_at"));
    return entry;
  }

  public static class BlacklistEntry {
    private Long id;
    private String targetType;
    private String targetValue;
    private String reason;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetValue() { return targetValue; }
    public void setTargetValue(String targetValue) { this.targetValue = targetValue; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
  }
}