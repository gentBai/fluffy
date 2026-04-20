package com.halfhex.fluffy.gateway;

import com.halfhex.fluffy.entity.ServiceInstance;
import com.halfhex.fluffy.repository.ServiceRepository;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

import java.util.HashSet;
import java.util.Set;

/**
 * Forwards HTTP requests to backend service instances.
 *
 * <p>Handles the actual HTTP forwarding with proper header handling:
 * <ul>
 *   <li>Adds X-Forwarded-* headers for proxy information</li>
 *   <li>Strips hop-by-hop headers that should not be forwarded</li>
 *   <li>Sets a 5-second timeout for all requests</li>
 * </ul>
 *
 * @author fluffy
 */
public class BackendClient {

    private static final Set<String> HOP_BY_HOP_HEADERS = new HashSet<>();

    static {
        HOP_BY_HOP_HEADERS.add("connection");
        HOP_BY_HOP_HEADERS.add("keep-alive");
        HOP_BY_HOP_HEADERS.add("proxy-authenticate");
        HOP_BY_HOP_HEADERS.add("proxy-authorization");
        HOP_BY_HOP_HEADERS.add("te");
        HOP_BY_HOP_HEADERS.add("trailers");
        HOP_BY_HOP_HEADERS.add("transfer-encoding");
        HOP_BY_HOP_HEADERS.add("upgrade");
    }

    private final io.vertx.core.Vertx vertx;
    private final ServiceRepository serviceRepository;
    private final WebClient webClient;

    public BackendClient(io.vertx.core.Vertx vertx, ServiceRepository serviceRepository) {
        this.vertx = vertx;
        this.serviceRepository = serviceRepository;
        this.webClient = WebClient.create(vertx);
    }

    public Future<HttpResponse<Buffer>> forward(HttpServerRequest request, ServiceInstance instance, String path) {
        Promise<HttpResponse<Buffer>> promise = Promise.promise();

        SocketAddress serverAddress = SocketAddress.inetSocketAddress(instance.getPort(), instance.getHost());
        HttpMethod method = request.method();

        io.vertx.ext.web.client.HttpRequest<Buffer> httpRequest = webClient.requestAbs(method, serverAddress, path);

        httpRequest.putHeader("X-Forwarded-For", request.remoteAddress().host());
        httpRequest.putHeader("X-Forwarded-Host", request.host());
        httpRequest.putHeader("X-Forwarded-Proto", request.scheme());

        request.headers().forEach(header -> {
            if (!HOP_BY_HOP_HEADERS.contains(header.getKey().toLowerCase())) {
                httpRequest.putHeader(header.getKey(), header.getValue());
            }
        });

        httpRequest.timeout(5000);

        if (method == HttpMethod.GET || method == HttpMethod.HEAD) {
            httpRequest.send(promise);
        } else {
            httpRequest.sendBuffer(Buffer.buffer(), promise);
        }

        return promise.future();
    }
}
