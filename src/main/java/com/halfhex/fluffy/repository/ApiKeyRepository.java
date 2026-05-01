package com.halfhex.fluffy.repository;

import com.halfhex.fluffy.entity.ApiKey;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ApiKeyRepository {

  private final Pool client;

  public ApiKeyRepository(Pool client) {
    this.client = client;
  }

  public void save(ApiKey entity, Promise<ApiKey> promise) {
    client.withConnection(conn ->
      conn.preparedQuery(
        "INSERT INTO api_key (key_value, user_id, name, route_ids, rate_limit_per_minute, active, expires_at, deleted, created_at, updated_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
      ).execute(Tuple.of(
        entity.getKeyValue(),
        entity.getUserId(),
        entity.getName(),
        entity.getRouteIds(),
        entity.getRateLimitPerMinute() != null ? entity.getRateLimitPerMinute() : 1000,
        entity.getActive() != null ? entity.getActive() : true,
        entity.getExpiresAt(),
        entity.getDeleted() != null ? entity.getDeleted() : false,
        LocalDateTime.now(),
        LocalDateTime.now()
      )).map(rowSet -> entity)
    ).onComplete(ar -> {
      if (ar.succeeded()) promise.complete(ar.result());
      else promise.fail(ar.cause());
    });
  }

  public void findById(Long id, Promise<ApiKey> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("SELECT * FROM api_key WHERE id = ? AND deleted = 0")
        .execute(Tuple.of(id))
        .map(rowSet -> {
          if (!rowSet.iterator().hasNext()) return null;
          return mapRowToEntity(rowSet.iterator().next());
        })
    ).onComplete(ar -> {
      if (ar.succeeded()) {
        if (ar.result() == null) promise.fail("ApiKey not found");
        else promise.complete(ar.result());
      } else promise.fail(ar.cause());
    });
  }

  public void findByKeyValue(String keyValue, Promise<ApiKey> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("SELECT * FROM api_key WHERE key_value = ? AND deleted = 0")
        .execute(Tuple.of(keyValue))
        .map(rowSet -> {
          if (!rowSet.iterator().hasNext()) return null;
          return mapRowToEntity(rowSet.iterator().next());
        })
    ).onComplete(ar -> {
      if (ar.succeeded()) {
        if (ar.result() == null) promise.fail("ApiKey not found");
        else promise.complete(ar.result());
      } else promise.fail(ar.cause());
    });
  }

  public void findByUserId(Long userId, Promise<List<ApiKey>> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("SELECT * FROM api_key WHERE user_id = ? AND deleted = 0 ORDER BY created_at DESC")
        .execute(Tuple.of(userId))
        .map(rowSet -> {
          List<ApiKey> list = new ArrayList<>();
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

  public void findAll(Promise<List<ApiKey>> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("SELECT * FROM api_key WHERE deleted = 0 ORDER BY created_at DESC")
        .execute()
        .map(rowSet -> {
          List<ApiKey> list = new ArrayList<>();
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

  public void update(ApiKey entity, Promise<ApiKey> promise) {
    client.withConnection(conn ->
      conn.preparedQuery(
        "UPDATE api_key SET key_value = ?, user_id = ?, name = ?, route_ids = ?, " +
        "rate_limit_per_minute = ?, active = ?, expires_at = ?, updated_at = ? WHERE id = ? AND deleted = 0"
      ).execute(Tuple.of(
        entity.getKeyValue(),
        entity.getUserId(),
        entity.getName(),
        entity.getRouteIds(),
        entity.getRateLimitPerMinute(),
        entity.getActive(),
        entity.getExpiresAt(),
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
      conn.preparedQuery("UPDATE api_key SET deleted = 1, updated_at = ? WHERE id = ?")
        .execute(Tuple.of(LocalDateTime.now(), id))
        .mapEmpty()
    ).onComplete(ar -> {
      if (ar.succeeded()) promise.complete();
      else promise.fail(ar.cause());
    });
  }

  private ApiKey mapRowToEntity(Row row) {
    if (row == null) return null;
    ApiKey apiKey = new ApiKey();
    apiKey.setId(row.getLong("id"));
    apiKey.setKeyValue(row.getString("key_value"));
    apiKey.setUserId(row.getLong("user_id"));
    apiKey.setName(row.getString("name"));
    apiKey.setRouteIds(row.getString("route_ids"));
    apiKey.setRateLimitPerMinute(row.getInteger("rate_limit_per_minute"));
    apiKey.setActive(row.getBoolean("active"));
    apiKey.setExpiresAt(row.getLocalDateTime("expires_at"));
    apiKey.setDeleted(row.getBoolean("deleted"));
    apiKey.setCreatedAt(row.getLocalDateTime("created_at"));
    apiKey.setUpdatedAt(row.getLocalDateTime("updated_at"));
    return apiKey;
  }
}
