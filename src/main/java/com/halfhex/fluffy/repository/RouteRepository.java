package com.halfhex.fluffy.repository;

import com.halfhex.fluffy.entity.GatewayRoute;
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

/**
 * Repository for GatewayRoute entity operations.
 *
 * @author fluffy
 */
public class RouteRepository {

  private final MySQLPool client;

  public RouteRepository(MySQLPool client) {
    this.client = client;
  }

  public void save(GatewayRoute route, Promise<Long> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      String sql = "INSERT INTO gateway_route (name, path_pattern, http_method, service_id, " +
                   "auth_required, rate_limit_enabled, priority, deleted, created_at, updated_at) " +
                   "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
      connection.preparedQuery(sql)
        .execute(Tuple.of(
          route.getName(),
          route.getPathPattern(),
          route.getHttpMethod() != null ? route.getHttpMethod() : "*",
          route.getServiceId(),
          route.getAuthRequired() ? 1 : 0,
          route.getRateLimitEnabled() ? 1 : 0,
          route.getPriority(),
          route.getDeleted() ? 1 : 0,
          route.getCreatedAt(),
          route.getUpdatedAt()
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

  public void update(GatewayRoute route, Promise<Void> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      String sql = "UPDATE gateway_route SET name = ?, path_pattern = ?, http_method = ?, " +
                   "service_id = ?, auth_required = ?, rate_limit_enabled = ?, priority = ?, " +
                   "deleted = ?, updated_at = ? WHERE id = ?";
      connection.preparedQuery(sql)
        .execute(Tuple.of(
          route.getName(),
          route.getPathPattern(),
          route.getHttpMethod() != null ? route.getHttpMethod() : "*",
          route.getServiceId(),
          route.getAuthRequired() ? 1 : 0,
          route.getRateLimitEnabled() ? 1 : 0,
          route.getPriority(),
          route.getDeleted() ? 1 : 0,
          LocalDateTime.now(),
          route.getId()
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

  public void softDelete(Long id, Promise<Void> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("UPDATE gateway_route SET deleted = 1, updated_at = ? WHERE id = ?")
        .execute(Tuple.of(LocalDateTime.now(), id), ar -> {
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

  public void findById(Long id, Promise<GatewayRoute> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery("SELECT * FROM gateway_route WHERE id = ? AND deleted = 0")
        .execute(Tuple.of(id), ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          Row row = ar.result().iterator().next();
          promise.complete(row == null ? null : mapRowToEntity(row));
          connection.close();
        });
    });
  }

  public void findByPathAndMethod(String path, String method, Promise<GatewayRoute> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery(
        "SELECT * FROM gateway_route WHERE path_pattern = ? AND http_method = ? AND deleted = 0")
        .execute(Tuple.of(path, method), ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          Row row = ar.result().iterator().next();
          promise.complete(row == null ? null : mapRowToEntity(row));
          connection.close();
        });
    });
  }

  public void findAllEnabled(Promise<List<GatewayRoute>> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery(
        "SELECT * FROM gateway_route WHERE deleted = 0 ORDER BY priority DESC")
        .execute(ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          List<GatewayRoute> routes = new ArrayList<>();
          for (Row row : ar.result()) {
            routes.add(mapRowToEntity(row));
          }
          promise.complete(routes);
          connection.close();
        });
    });
  }

  public void findAll(Promise<List<GatewayRoute>> promise) {
    client.getConnection(conn -> {
      if (conn.failed()) {
        promise.fail(conn.cause());
        return;
      }
      SqlConnection connection = conn.result();
      connection.preparedQuery(
        "SELECT * FROM gateway_route WHERE deleted = 0 ORDER BY priority DESC")
        .execute(ar -> {
          if (ar.failed()) {
            promise.fail(ar.cause());
            connection.close();
            return;
          }
          List<GatewayRoute> routes = new ArrayList<>();
          for (Row row : ar.result()) {
            routes.add(mapRowToEntity(row));
          }
          promise.complete(routes);
          connection.close();
        });
    });
  }

  private GatewayRoute mapRowToEntity(Row row) {
    return GatewayRoute.builder()
      .id(row.getLong("id"))
      .name(row.getString("name"))
      .pathPattern(row.getString("path_pattern"))
      .httpMethod(row.getString("http_method"))
      .serviceId(row.getLong("service_id"))
      .authRequired(row.getInteger("auth_required") == 1)
      .rateLimitEnabled(row.getInteger("rate_limit_enabled") == 1)
      .priority(row.getInteger("priority"))
      .deleted(row.getInteger("deleted") == 1)
      .createdAt(row.getLocalDateTime("created_at"))
      .updatedAt(row.getLocalDateTime("updated_at"))
      .build();
  }
}
