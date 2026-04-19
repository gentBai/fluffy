package com.halfhex.fluffy.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class GatewayRouteTest {

  @Test
  void testBuilder_allFields() {
    LocalDateTime now = LocalDateTime.now();
    GatewayRoute route = GatewayRoute.builder()
      .id(1L)
      .name("Test Route")
      .pathPattern("/api/users")
      .httpMethod(GatewayRoute.HttpMethod.GET)
      .serviceId(10L)
      .authRequired(true)
      .rateLimitEnabled(true)
      .priority(5)
      .deleted(false)
      .createdAt(now)
      .updatedAt(now)
      .build();

    assertEquals(1L, route.getId());
    assertEquals("Test Route", route.getName());
    assertEquals("/api/users", route.getPathPattern());
    assertEquals(GatewayRoute.HttpMethod.GET, route.getHttpMethod());
    assertEquals(10L, route.getServiceId());
    assertTrue(route.getAuthRequired());
    assertTrue(route.getRateLimitEnabled());
    assertEquals(5, route.getPriority());
    assertFalse(route.getDeleted());
    assertEquals(now, route.getCreatedAt());
    assertEquals(now, route.getUpdatedAt());
  }

  @Test
  void testBuilder_partialFields() {
    GatewayRoute route = GatewayRoute.builder()
      .name("Partial Route")
      .pathPattern("/api/*")
      .build();

    assertNull(route.getId());
    assertEquals("Partial Route", route.getName());
    assertEquals("/api/*", route.getPathPattern());
    assertNull(route.getHttpMethod());
    assertNull(route.getServiceId());
    assertNull(route.getAuthRequired());
  }

  @Test
  void testHttpMethod_enumValues() {
    assertEquals(7, GatewayRoute.HttpMethod.values().length);
    assertNotNull(GatewayRoute.HttpMethod.valueOf("GET"));
    assertNotNull(GatewayRoute.HttpMethod.valueOf("POST"));
    assertNotNull(GatewayRoute.HttpMethod.valueOf("PUT"));
    assertNotNull(GatewayRoute.HttpMethod.valueOf("DELETE"));
    assertNotNull(GatewayRoute.HttpMethod.valueOf("PATCH"));
    assertNotNull(GatewayRoute.HttpMethod.valueOf("OPTIONS"));
    assertNotNull(GatewayRoute.HttpMethod.valueOf("HEAD"));
  }

  @Test
  void testBuilder_chaining() {
    GatewayRoute route = GatewayRoute.builder()
      .id(1L)
      .name("Route 1")
      .pathPattern("/test")
      .httpMethod(GatewayRoute.HttpMethod.POST)
      .serviceId(1L)
      .authRequired(false)
      .rateLimitEnabled(false)
      .priority(1)
      .deleted(false)
      .createdAt(LocalDateTime.now())
      .updatedAt(LocalDateTime.now())
      .build();

    assertNotNull(route);
  }
}
