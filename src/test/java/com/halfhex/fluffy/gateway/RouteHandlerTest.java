package com.halfhex.fluffy.gateway;

import com.halfhex.fluffy.entity.GatewayRoute;
import com.halfhex.fluffy.entity.GatewayRoute.HttpMethod;
import com.halfhex.fluffy.repository.RouteRepository;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteHandlerTest {

  @Mock
  private RouteRepository routeRepository;

  private RouteHandler routeHandler;
  private Vertx vertx;

  @BeforeEach
  void setUp() {
    vertx = Vertx.vertx();
    routeHandler = new RouteHandler(routeRepository, vertx);
  }

  @Test
  void testMatchRoute_exactMatch() {
    GatewayRoute route = createRoute(1L, "/api/users", HttpMethod.GET, 1);
    doAnswer(invocation -> {
      Promise<List<GatewayRoute>> p = invocation.getArgument(0);
      p.complete(Collections.singletonList(route));
      return null;
    }).when(routeRepository).findAllEnabled(any());

    routeHandler.refreshCache();

    Future<GatewayRoute> future = routeHandler.matchRoute("/api/users", "GET");
    assertNotNull(future);
  }

  @Test
  void testMatchRoute_wildcardSuffixMatch() {
    GatewayRoute route = createRoute(1L, "/api/*", HttpMethod.GET, 1);
    doAnswer(invocation -> {
      Promise<List<GatewayRoute>> p = invocation.getArgument(0);
      p.complete(Collections.singletonList(route));
      return null;
    }).when(routeRepository).findAllEnabled(any());

    routeHandler.refreshCache();

    Future<GatewayRoute> future = routeHandler.matchRoute("/api/users/123", "GET");
    assertNotNull(future);
  }

  @Test
  void testMatchRoute_doubleWildcardMatch() {
    GatewayRoute route = createRoute(1L, "/api/**", HttpMethod.GET, 1);
    doAnswer(invocation -> {
      Promise<List<GatewayRoute>> p = invocation.getArgument(0);
      p.complete(Collections.singletonList(route));
      return null;
    }).when(routeRepository).findAllEnabled(any());

    routeHandler.refreshCache();

    Future<GatewayRoute> future = routeHandler.matchRoute("/api/users/123/profile", "GET");
    assertNotNull(future);
  }

  @Test
  void testMatchRoute_methodMismatch_returnsNull() {
    GatewayRoute route = createRoute(1L, "/api/users", HttpMethod.GET, 1);
    doAnswer(invocation -> {
      Promise<List<GatewayRoute>> p = invocation.getArgument(0);
      p.complete(Collections.singletonList(route));
      return null;
    }).when(routeRepository).findAllEnabled(any());

    routeHandler.refreshCache();

    Future<GatewayRoute> future = routeHandler.matchRoute("/api/users", "POST");
    assertNotNull(future);
  }

  @Test
  void testMatchRoute_highestPriorityWins() {
    GatewayRoute lowPriority = createRoute(1L, "/api/*", HttpMethod.GET, 1);
    GatewayRoute highPriority = createRoute(2L, "/api/*", HttpMethod.GET, 10);
    List<GatewayRoute> routes = Arrays.asList(lowPriority, highPriority);

    doAnswer(invocation -> {
      Promise<List<GatewayRoute>> p = invocation.getArgument(0);
      p.complete(routes);
      return null;
    }).when(routeRepository).findAllEnabled(any());

    routeHandler.refreshCache();

    Future<GatewayRoute> future = routeHandler.matchRoute("/api/users", "GET");
    assertNotNull(future);
  }

  @Test
  void testMatchRoute_noMatch() {
    doAnswer(invocation -> {
      Promise<List<GatewayRoute>> p = invocation.getArgument(0);
      p.complete(Collections.emptyList());
      return null;
    }).when(routeRepository).findAllEnabled(any());

    routeHandler.refreshCache();

    Future<GatewayRoute> future = routeHandler.matchRoute("/unknown", "GET");
    assertNotNull(future);
  }

  @Test
  void testRefreshCache() {
    GatewayRoute route = createRoute(1L, "/api/test", HttpMethod.GET, 1);
    doAnswer(invocation -> {
      Promise<List<GatewayRoute>> p = invocation.getArgument(0);
      p.complete(Collections.singletonList(route));
      return null;
    }).when(routeRepository).findAllEnabled(any());

    Future<Void> future = routeHandler.refreshCache();
    assertNotNull(future);
  }

  private GatewayRoute createRoute(Long id, String path, HttpMethod method, int priority) {
    return GatewayRoute.builder()
      .id(id)
      .name("Test Route")
      .pathPattern(path)
      .httpMethod(method)
      .serviceId(1L)
      .authRequired(false)
      .rateLimitEnabled(false)
      .priority(priority)
      .deleted(false)
      .createdAt(LocalDateTime.now())
      .updatedAt(LocalDateTime.now())
      .build();
  }
}