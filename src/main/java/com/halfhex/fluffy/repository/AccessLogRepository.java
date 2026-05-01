package com.halfhex.fluffy.repository;

import com.halfhex.fluffy.entity.AccessLog;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Tuple;

import java.time.LocalDateTime;

public class AccessLogRepository {

  private final Pool client;

  public AccessLogRepository(Pool client) {
    this.client = client;
  }

  public void save(AccessLog entity, Promise<Void> promise) {
    client.withConnection(conn ->
      conn.preparedQuery(
        "INSERT INTO access_log (trace_id, request_method, request_path, request_query, " +
        "request_body, response_status, response_time_ms, client_ip, user_id, api_key_id, " +
        "route_id, service_id, instance_id, error_message, created_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
      ).execute(Tuple.of(
        entity.getTraceId(),
        entity.getRequestMethod(),
        entity.getRequestPath(),
        entity.getRequestQuery(),
        entity.getRequestBody(),
        entity.getResponseStatus(),
        entity.getResponseTimeMs(),
        entity.getClientIp(),
        entity.getUserId(),
        entity.getApiKeyId(),
        entity.getRouteId(),
        entity.getServiceId(),
        entity.getInstanceId(),
        entity.getErrorMessage(),
        LocalDateTime.now()
      )).mapEmpty()
    ).onComplete(ar -> {
      if (ar.succeeded()) promise.complete();
      else promise.fail(ar.cause());
    });
  }
}
