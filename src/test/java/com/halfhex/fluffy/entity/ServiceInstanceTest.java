package com.halfhex.fluffy.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ServiceInstanceTest {

  @Test
  void testBuilder_allFields() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime heartbeat = LocalDateTime.now().minusMinutes(5);

    ServiceInstance instance = ServiceInstance.builder()
      .id(1L)
      .serviceId(10L)
      .host("192.168.1.100")
      .port(8080)
      .status(ServiceInstance.Status.HEALTHY)
      .enabled(true)
      .lastHeartbeat(heartbeat)
      .createdAt(now)
      .updatedAt(now)
      .build();

    assertEquals(1L, instance.getId());
    assertEquals(10L, instance.getServiceId());
    assertEquals("192.168.1.100", instance.getHost());
    assertEquals(8080, instance.getPort());
    assertEquals(ServiceInstance.Status.HEALTHY, instance.getStatus());
    assertTrue(instance.getEnabled());
    assertEquals(heartbeat, instance.getLastHeartbeat());
    assertEquals(now, instance.getCreatedAt());
    assertEquals(now, instance.getUpdatedAt());
  }

  @Test
  void testBuilder_partialFields() {
    ServiceInstance instance = ServiceInstance.builder()
      .host("localhost")
      .port(8080)
      .build();

    assertNull(instance.getId());
    assertEquals("localhost", instance.getHost());
    assertEquals(8080, instance.getPort());
    assertNull(instance.getServiceId());
    assertNull(instance.getStatus());
  }

  @Test
  void testStatus_enumValues() {
    assertEquals(3, ServiceInstance.Status.values().length);
    assertNotNull(ServiceInstance.Status.valueOf("HEALTHY"));
    assertNotNull(ServiceInstance.Status.valueOf("UNHEALTHY"));
    assertNotNull(ServiceInstance.Status.valueOf("DOWN"));
  }

  @Test
  void testBuilder_chaining() {
    ServiceInstance instance = ServiceInstance.builder()
      .id(1L)
      .serviceId(1L)
      .host("10.0.0.1")
      .port(443)
      .status(ServiceInstance.Status.UNHEALTHY)
      .enabled(false)
      .createdAt(LocalDateTime.now())
      .updatedAt(LocalDateTime.now())
      .build();

    assertNotNull(instance);
    assertEquals(ServiceInstance.Status.UNHEALTHY, instance.getStatus());
    assertFalse(instance.getEnabled());
  }
}
