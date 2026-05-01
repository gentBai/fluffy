package com.halfhex.fluffy.repository;

import com.halfhex.fluffy.entity.GatewayRoute;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class GatewayRouteRepository {

  private final Pool client;

  public GatewayRouteRepository(Pool client) {
    this.client = client;
  }

  public void save(GatewayRoute entity, Promise<GatewayRoute> promise) {
    client.withConnection(conn ->
      conn.preparedQuery(
        "INSERT INTO gateway_route (name, path_pattern, http_method, service_id, auth_required, rate_limit_enabled, priority, deleted, created_at, updated_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
      ).execute(Tuple.of(
        entity.getName(),
        entity.getPathPattern(),
        entity.getHttpMethod(),
        entity.getServiceId(),
        entity.getAuthRequired(),
        entity.getRateLimitEnabled(),
        entity.getPriority(),
        entity.getDeleted() != null ? entity.getDeleted() : false,
        LocalDateTime.now(),
        LocalDateTime.now()
      )).map(rowSet -> entity)
    ).onComplete(ar -> {
      if (ar.succeeded()) promise.complete(ar.result());
      else promise.fail(ar.cause());
    });
  }

  public void findById(Long id, Promise<GatewayRoute> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("SELECT * FROM gateway_route WHERE id = ? AND deleted = 0")
        .execute(Tuple.of(id))
        .map(rowSet -> {
          if (!rowSet.iterator().hasNext()) return null;
          return mapRowToEntity(rowSet.iterator().next());
        })
    ).onComplete(ar -> {
      if (ar.succeeded()) {
        if (ar.result() == null) promise.fail("GatewayRoute not found");
        else promise.complete(ar.result());
      } else promise.fail(ar.cause());
    });
  }

  public void findAll(Promise<List<GatewayRoute>> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("SELECT * FROM gateway_route WHERE deleted = 0 ORDER BY priority")
        .execute()
        .map(rowSet -> {
          java.util.ArrayList<GatewayRoute> list = new java.util.ArrayList<>();
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

  public void update(GatewayRoute entity, Promise<GatewayRoute> promise) {
    client.withConnection(conn ->
      conn.preparedQuery(
        "UPDATE gateway_route SET name = ?, path_pattern = ?, http_method = ?, service_id = ?, " +
        "auth_required = ?, rate_limit_enabled = ?, priority = ?, updated_at = ? " +
        "WHERE id = ? AND deleted = 0"
      ).execute(Tuple.of(
        entity.getName(),
        entity.getPathPattern(),
        entity.getHttpMethod(),
        entity.getServiceId(),
        entity.getAuthRequired(),
        entity.getRateLimitEnabled(),
        entity.getPriority(),
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
      conn.preparedQuery("UPDATE gateway_route SET deleted = 1, updated_at = ? WHERE id = ?")
        .execute(Tuple.of(LocalDateTime.now(), id))
        .mapEmpty()
    ).onComplete(ar -> {
      if (ar.succeeded()) promise.complete();
      else promise.fail(ar.cause());
    });
  }

  private GatewayRoute mapRowToEntity(Row row) {
    if (row == null) return null;
    return GatewayRoute.builder()
      .id(row.getLong("id"))
      .name(row.getString("name"))
      .pathPattern(row.getString("path_pattern"))
      .httpMethod(row.getString("http_method"))
      .serviceId(row.getLong("service_id"))
      .authRequired(row.getBoolean("auth_required"))
      .rateLimitEnabled(row.getBoolean("rate_limit_enabled"))
      .priority(row.getInteger("priority"))
      .deleted(row.getBoolean("deleted"))
      .createdAt(row.getLocalDateTime("created_at"))
      .updatedAt(row.getLocalDateTime("updated_at"))
      .build();
  }
}