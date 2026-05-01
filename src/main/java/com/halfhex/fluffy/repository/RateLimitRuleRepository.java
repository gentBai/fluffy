package com.halfhex.fluffy.repository;

import com.halfhex.fluffy.entity.RateLimitRule;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RateLimitRuleRepository {

  private final Pool client;

  public RateLimitRuleRepository(Pool client) {
    this.client = client;
  }

  public void save(RateLimitRule entity, Promise<RateLimitRule> promise) {
    client.withConnection(conn ->
      conn.preparedQuery(
        "INSERT INTO rate_limit_rule (name, route_id, service_id, limit_type, max_requests, " +
        "requests_per_minute, requests_per_hour, requests_per_day, burst_size, deleted, created_at, updated_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
      ).execute(Tuple.of(
        entity.getName(),
        entity.getRouteId(),
        entity.getServiceId(),
        entity.getLimitType(),
        entity.getMaxRequests(),
        entity.getRequestsPerMinute(),
        entity.getRequestsPerHour(),
        entity.getRequestsPerDay(),
        entity.getBurstSize(),
        entity.getDeleted() != null ? entity.getDeleted() : false,
        LocalDateTime.now(),
        LocalDateTime.now()
      )).map(rowSet -> entity)
    ).onComplete(ar -> {
      if (ar.succeeded()) promise.complete(ar.result());
      else promise.fail(ar.cause());
    });
  }

  public void findById(Long id, Promise<RateLimitRule> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("SELECT * FROM rate_limit_rule WHERE id = ? AND deleted = 0")
        .execute(Tuple.of(id))
        .map(rowSet -> {
          if (!rowSet.iterator().hasNext()) return null;
          return mapRowToEntity(rowSet.iterator().next());
        })
    ).onComplete(ar -> {
      if (ar.succeeded()) {
        if (ar.result() == null) promise.fail("RateLimitRule not found");
        else promise.complete(ar.result());
      } else promise.fail(ar.cause());
    });
  }

  public void findByRouteId(Long routeId, Promise<RateLimitRule> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("SELECT * FROM rate_limit_rule WHERE route_id = ? AND deleted = 0")
        .execute(Tuple.of(routeId))
        .map(rowSet -> {
          if (!rowSet.iterator().hasNext()) return null;
          return mapRowToEntity(rowSet.iterator().next());
        })
    ).onComplete(ar -> {
      if (ar.succeeded()) {
        if (ar.result() == null) promise.fail("RateLimitRule not found for route");
        else promise.complete(ar.result());
      } else promise.fail(ar.cause());
    });
  }

  public void findByServiceId(Long serviceId, Promise<List<RateLimitRule>> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("SELECT * FROM rate_limit_rule WHERE service_id = ? AND deleted = 0")
        .execute(Tuple.of(serviceId))
        .map(rowSet -> {
          List<RateLimitRule> list = new ArrayList<>();
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

  public void findAll(Promise<List<RateLimitRule>> promise) {
    client.withConnection(conn ->
      conn.preparedQuery("SELECT * FROM rate_limit_rule WHERE deleted = 0 ORDER BY priority")
        .execute()
        .map(rowSet -> {
          List<RateLimitRule> list = new ArrayList<>();
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

  public void update(RateLimitRule entity, Promise<RateLimitRule> promise) {
    client.withConnection(conn ->
      conn.preparedQuery(
        "UPDATE rate_limit_rule SET name = ?, route_id = ?, service_id = ?, limit_type = ?, " +
        "max_requests = ?, requests_per_minute = ?, requests_per_hour = ?, requests_per_day = ?, " +
        "burst_size = ?, updated_at = ? WHERE id = ? AND deleted = 0"
      ).execute(Tuple.of(
        entity.getName(),
        entity.getRouteId(),
        entity.getServiceId(),
        entity.getLimitType(),
        entity.getMaxRequests(),
        entity.getRequestsPerMinute(),
        entity.getRequestsPerHour(),
        entity.getRequestsPerDay(),
        entity.getBurstSize(),
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
      conn.preparedQuery("UPDATE rate_limit_rule SET deleted = 1, updated_at = ? WHERE id = ?")
        .execute(Tuple.of(LocalDateTime.now(), id))
        .mapEmpty()
    ).onComplete(ar -> {
      if (ar.succeeded()) promise.complete();
      else promise.fail(ar.cause());
    });
  }

  private RateLimitRule mapRowToEntity(Row row) {
    if (row == null) return null;
    return new RateLimitRule() {{
      setId(row.getLong("id"));
      setName(row.getString("name"));
      setRouteId(row.getLong("route_id"));
      setServiceId(row.getLong("service_id"));
      setLimitType(row.getString("limit_type"));
      setMaxRequests(row.getInteger("max_requests"));
      setRequestsPerMinute(row.getInteger("requests_per_minute"));
      setRequestsPerHour(row.getInteger("requests_per_hour"));
      setRequestsPerDay(row.getInteger("requests_per_day"));
      setBurstSize(row.getInteger("burst_size"));
      setDeleted(row.getBoolean("deleted"));
      setCreatedAt(row.getLocalDateTime("created_at"));
      setUpdatedAt(row.getLocalDateTime("updated_at"));
    }};
  }
}