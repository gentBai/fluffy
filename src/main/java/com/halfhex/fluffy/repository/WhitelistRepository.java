package com.halfhex.fluffy.repository;

import com.halfhex.fluffy.entity.Whitelist;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WhitelistRepository {

  private final Pool client;

  public WhitelistRepository(Pool client) {
    this.client = client;
  }

  public void save(Whitelist entity, Promise<Whitelist> promise) {
    client.withConnection(conn ->
      conn.preparedQuery(
        "INSERT INTO whitelist (target_type, target_value, description, deleted, created_at, updated_at) " +
        "VALUES (?, ?, ?, ?, ?, ?)"
      ).execute(Tuple.of(
        entity.getTargetType(),
        entity.getTargetValue(),
        entity.getDescription(),
        entity.getDeleted() != null ? entity.getDeleted() : false,
        LocalDateTime.now(),
        LocalDateTime.now()
      )).map(rowSet -> entity)
    ).onComplete(ar -> {
      if (ar.succeeded()) promise.complete(ar.result());
      else promise.fail(ar.cause());
    });
  }

  public void findById(Long id, Promise<Whitelist> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("SELECT * FROM whitelist WHERE id = ? AND deleted = 0")
        .execute(Tuple.of(id))
        .map(rowSet -> {
          if (!rowSet.iterator().hasNext()) return null;
          return mapRowToEntity(rowSet.iterator().next());
        })
    ).onComplete(ar -> {
      if (ar.succeeded()) {
        if (ar.result() == null) promise.fail("Whitelist entry not found");
        else promise.complete(ar.result());
      } else promise.fail(ar.cause());
    });
  }

  public void findByTarget(String targetType, String targetValue, Promise<Whitelist> promise) {
    client.withConnection(conn ->
      conn.preparedQuery(
        "SELECT * FROM whitelist WHERE target_type = ? AND target_value = ? AND deleted = 0"
      ).execute(Tuple.of(targetType, targetValue))
      .map(rowSet -> {
        if (!rowSet.iterator().hasNext()) return null;
        return mapRowToEntity(rowSet.iterator().next());
      })
    ).onComplete(ar -> {
      if (ar.succeeded()) {
        if (ar.result() == null) promise.fail("Whitelist entry not found");
        else promise.complete(ar.result());
      } else promise.fail(ar.cause());
    });
  }

  public void findAll(Promise<List<Whitelist>> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("SELECT * FROM whitelist WHERE deleted = 0 ORDER BY created_at DESC")
        .execute()
        .map(rowSet -> {
          List<Whitelist> list = new ArrayList<>();
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

  public void findByType(String targetType, Promise<List<Whitelist>> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("SELECT * FROM whitelist WHERE target_type = ? AND deleted = 0")
        .execute(Tuple.of(targetType))
        .map(rowSet -> {
          List<Whitelist> list = new ArrayList<>();
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

  public void update(Whitelist entity, Promise<Whitelist> promise) {
    client.withConnection(conn ->
      conn.preparedQuery(
        "UPDATE whitelist SET target_type = ?, target_value = ?, description = ?, " +
        "updated_at = ? WHERE id = ? AND deleted = 0"
      ).execute(Tuple.of(
        entity.getTargetType(),
        entity.getTargetValue(),
        entity.getDescription(),
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
      conn.preparedQuery("UPDATE whitelist SET deleted = 1, updated_at = ? WHERE id = ?")
        .execute(Tuple.of(LocalDateTime.now(), id))
        .mapEmpty()
    ).onComplete(ar -> {
      if (ar.succeeded()) promise.complete();
      else promise.fail(ar.cause());
    });
  }

  private Whitelist mapRowToEntity(Row row) {
    if (row == null) return null;
    return new Whitelist() {{
      setId(row.getLong("id"));
      setTargetType(row.getString("target_type"));
      setTargetValue(row.getString("target_value"));
      setDescription(row.getString("description"));
      setDeleted(row.getBoolean("deleted"));
      setCreatedAt(row.getLocalDateTime("created_at"));
      setUpdatedAt(row.getLocalDateTime("updated_at"));
    }};
  }
}