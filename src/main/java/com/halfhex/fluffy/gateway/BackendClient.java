package com.halfhex.fluffy.gateway;

import com.halfhex.fluffy.config.ConfigHolder;
import com.halfhex.fluffy.entity.ServiceInstance;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BackendClient {

  public static final Set<String> HOP_BY_HOP_HEADERS;

  static {
    Set<String> headers = new HashSet<>();
    headers.add("connection");
    headers.add("keep-alive");
    headers.add("proxy-authenticate");
    headers.add("proxy-authorization");
    headers.add("te");
    headers.add("trailers");
    headers.add("transfer-encoding");
    headers.add("upgrade");
    HOP_BY_HOP_HEADERS = Collections.unmodifiableSet(headers);
  }

  private final io.vertx.core.Vertx vertx;
  private final WebClient webClient;
  private final ConfigHolder configHolder;

  public BackendClient(io.vertx.core.Vertx vertx, ConfigHolder configHolder) {
    this.vertx = vertx;
    this.configHolder = configHolder;
    this.webClient = WebClient.create(vertx);
  }

  public Future<HttpResponse<Buffer>> forward(HttpServerRequest request, ServiceInstance instance, String path) {
    HttpMethod method = request.method();

    io.vertx.ext.web.client.HttpRequest<Buffer> httpRequest = webClient
      .request(method, instance.getPort(), instance.getHost(), path);

    httpRequest.putHeader("X-Forwarded-For", request.remoteAddress().host());
    httpRequest.putHeader("X-Forwarded-Host", request.getHeader("host"));
    httpRequest.putHeader("X-Forwarded-Proto", request.scheme());

    request.headers().forEach(header -> {
      if (!HOP_BY_HOP_HEADERS.contains(header.getKey().toLowerCase())) {
        httpRequest.putHeader(header.getKey(), header.getValue());
      }
    });

    httpRequest.timeout(configHolder.getGatewayRequestTimeout());

    if (method == HttpMethod.GET || method == HttpMethod.HEAD) {
      return httpRequest.send();
    } else {
      return request.body().compose(body -> httpRequest.sendBuffer(body));
    }
  }
}
