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

public class ApiKeyRepository {

  private final MySQLPool client;

  public ApiKeyRepository(MySQLPool client) {
    this.client = client;
  }

  public void save(ApiKey apiKey, Promise<Long> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("INSERT INTO api_key (key_value, user_id, secret, name, active, expires_at, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")
        .execute(Tuple.of(
          apiKey.getKeyValue(),
          apiKey.getUserId(),
          apiKey.getSecret(),
          apiKey.getName(),
          apiKey.isActive() ? 0 : 1,
          apiKey.getExpiresAt(),
          apiKey.getCreatedAt(),
          apiKey.getUpdatedAt()
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

  public void update(ApiKey apiKey, Promise<Void> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("UPDATE api_key SET key_value = ?, user_id = ?, secret = ?, name = ?, active = ?, expires_at = ?, updated_at = ? WHERE id = ?")
        .execute(Tuple.of(
          apiKey.getKeyValue(),
          apiKey.getUserId(),
          apiKey.getSecret(),
          apiKey.getName(),
          apiKey.isActive() ? 0 : 1,
          apiKey.getExpiresAt(),
          apiKey.getUpdatedAt(),
          apiKey.getId()
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
      connection.preparedQuery("UPDATE api_key SET deleted = 1, updated_at = ? WHERE id = ?")
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

  public void findById(Long id, Promise<ApiKey> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM api_key WHERE id = ? AND deleted = 0")
        .execute(Tuple.of(id), ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          Row row = ar.result().iterator().next();
          promise.complete(row == null ? null : mapRowToApiKey(row));
          connection.close();
        });
    });
  }

  public void findByKeyValue(String keyValue, Promise<ApiKey> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM api_key WHERE key_value = ? AND deleted = 0")
        .execute(Tuple.of(keyValue), ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          Row row = ar.result().iterator().next();
          promise.complete(row == null ? null : mapRowToApiKey(row));
          connection.close();
        });
    });
  }

  public void findActiveByUserId(Long userId, Promise<List<ApiKey>> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM api_key WHERE user_id = ? AND active = 0 AND deleted = 0 ORDER BY created_at DESC")
        .execute(Tuple.of(userId), ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          List<ApiKey> apiKeys = new ArrayList<>();
          for (Row row : ar.result()) {
            apiKeys.add(mapRowToApiKey(row));
          }
          promise.complete(apiKeys);
          connection.close();
        });
    });
  }

  public void findAll(Promise<List<ApiKey>> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM api_key WHERE deleted = 0 ORDER BY created_at DESC")
        .execute(ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          List<ApiKey> apiKeys = new ArrayList<>();
          for (Row row : ar.result()) {
            apiKeys.add(mapRowToApiKey(row));
          }
          promise.complete(apiKeys);
          connection.close();
        });
    });
  }

  private ApiKey mapRowToApiKey(Row row) {
    ApiKey apiKey = new ApiKey();
    apiKey.setId(row.getLong("id"));
    apiKey.setKeyValue(row.getString("key_value"));
    apiKey.setUserId(row.getLong("user_id"));
    apiKey.setSecret(row.getString("secret"));
    apiKey.setName(row.getString("name"));
    apiKey.setActive(row.getInteger("active") == 0);
    apiKey.setExpiresAt(row.getLocalDateTime("expires_at"));
    apiKey.setCreatedAt(row.getLocalDateTime("created_at"));
    apiKey.setUpdatedAt(row.getLocalDateTime("updated_at"));
    return apiKey;
  }

  public static class ApiKey {
    private Long id;
    private String keyValue;
    private Long userId;
    private String secret;
    private String name;
    private boolean active;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getKeyValue() { return keyValue; }
    public void setKeyValue(String keyValue) { this.keyValue = keyValue; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
  }
}