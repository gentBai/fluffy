package com.halfhex.fluffy.repository;

import com.halfhex.fluffy.entity.Blacklist;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BlacklistRepository {

  private final Pool client;

  public BlacklistRepository(Pool client) {
    this.client = client;
  }

  public void save(Blacklist entity, Promise<Blacklist> promise) {
    client.withConnection(conn ->
      conn.preparedQuery(
        "INSERT INTO blacklist (target_type, target_value, reason, expires_at, deleted, created_at, updated_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)"
      ).execute(Tuple.of(
        entity.getTargetType(),
        entity.getTargetValue(),
        entity.getReason(),
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

  public void findById(Long id, Promise<Blacklist> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("SELECT * FROM blacklist WHERE id = ? AND deleted = 0")
        .execute(Tuple.of(id))
        .map(rowSet -> {
          if (!rowSet.iterator().hasNext()) return null;
          return mapRowToEntity(rowSet.iterator().next());
        })
    ).onComplete(ar -> {
      if (ar.succeeded()) {
        if (ar.result() == null) promise.fail("Blacklist entry not found");
        else promise.complete(ar.result());
      } else promise.fail(ar.cause());
    });
  }

  public void findByTarget(String targetType, String targetValue, Promise<Blacklist> promise) {
    client.withConnection(conn ->
      conn.preparedQuery(
        "SELECT * FROM blacklist WHERE target_type = ? AND target_value = ? " +
        "AND deleted = 0 AND (expires_at IS NULL OR expires_at > NOW()) LIMIT 1"
      ).execute(Tuple.of(targetType, targetValue))
      .map(rowSet -> {
        if (!rowSet.iterator().hasNext()) return null;
        return mapRowToEntity(rowSet.iterator().next());
      })
    ).onComplete(ar -> {
      if (ar.succeeded()) {
        if (ar.result() == null) promise.fail("Blacklist entry not found");
        else promise.complete(ar.result());
      } else promise.fail(ar.cause());
    });
  }

  public void findAll(Promise<List<Blacklist>> promise) {
    client.withConnection(conn ->
      conn.preparedQuery(
        "SELECT * FROM blacklist WHERE deleted = 0 " +
        "AND (expires_at IS NULL OR expires_at > NOW()) ORDER BY created_at DESC"
      ).execute()
      .map(rowSet -> {
        List<Blacklist> list = new ArrayList<>();
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

  public void findAllIncludingExpired(Promise<List<Blacklist>> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("SELECT * FROM blacklist WHERE deleted = 0 ORDER BY created_at DESC")
        .execute()
        .map(rowSet -> {
          List<Blacklist> list = new ArrayList<>();
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

  public void update(Blacklist entity, Promise<Blacklist> promise) {
    client.withConnection(conn ->
      conn.preparedQuery(
        "UPDATE blacklist SET target_type = ?, target_value = ?, reason = ?, " +
        "expires_at = ?, updated_at = ? WHERE id = ? AND deleted = 0"
      ).execute(Tuple.of(
        entity.getTargetType(),
        entity.getTargetValue(),
        entity.getReason(),
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
      conn.preparedQuery("UPDATE blacklist SET deleted = 1, updated_at = ? WHERE id = ?")
        .execute(Tuple.of(LocalDateTime.now(), id))
        .mapEmpty()
    ).onComplete(ar -> {
      if (ar.succeeded()) promise.complete();
      else promise.fail(ar.cause());
    });
  }

  private Blacklist mapRowToEntity(Row row) {
    if (row == null) return null;
    return new Blacklist() {{
      setId(row.getLong("id"));
      setTargetType(row.getString("target_type"));
      setTargetValue(row.getString("target_value"));
      setReason(row.getString("reason"));
      setExpiresAt(row.getLocalDateTime("expires_at"));
      setDeleted(row.getBoolean("deleted"));
      setCreatedAt(row.getLocalDateTime("created_at"));
      setUpdatedAt(row.getLocalDateTime("updated_at"));
    }};
  }
}