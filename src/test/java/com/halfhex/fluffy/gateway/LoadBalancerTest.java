package com.halfhex.fluffy.gateway;

import com.halfhex.fluffy.entity.ServiceInstance;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LoadBalancerTest {

  @Test
  void testRoundRobin_equalWeights() throws Exception {
    LoadBalancer balancer = new LoadBalancer(null);
    Method method = LoadBalancer.class.getDeclaredMethod("selectRoundRobin", Long.class, List.class);
    method.setAccessible(true);

    ServiceInstance i1 = ServiceInstance.builder().id(1L).serviceId(1L).host("a").port(80).weight(1).healthy(true).enabled(true).build();
    ServiceInstance i2 = ServiceInstance.builder().id(2L).serviceId(1L).host("b").port(80).weight(1).healthy(true).enabled(true).build();
    List<ServiceInstance> instances = Arrays.asList(i1, i2);

    assertEquals(i1, method.invoke(balancer, 1L, instances));
    assertEquals(i2, method.invoke(balancer, 1L, instances));
    assertEquals(i1, method.invoke(balancer, 1L, instances));
    assertEquals(i2, method.invoke(balancer, 1L, instances));
  }

  @Test
  void testRoundRobin_withWeights() throws Exception {
    LoadBalancer balancer = new LoadBalancer(null);
    Method method = LoadBalancer.class.getDeclaredMethod("selectRoundRobin", Long.class, List.class);
    method.setAccessible(true);

    ServiceInstance i1 = ServiceInstance.builder().id(1L).serviceId(1L).host("a").port(80).weight(3).healthy(true).enabled(true).build();
    ServiceInstance i2 = ServiceInstance.builder().id(2L).serviceId(1L).host("b").port(80).weight(1).healthy(true).enabled(true).build();
    List<ServiceInstance> instances = Arrays.asList(i1, i2);

    // With weights 3:1, i1 should be selected 3 times, then i2 once
    assertEquals(i1, method.invoke(balancer, 1L, instances));
    assertEquals(i1, method.invoke(balancer, 1L, instances));
    assertEquals(i1, method.invoke(balancer, 1L, instances));
    assertEquals(i2, method.invoke(balancer, 1L, instances));
  }

  @Test
  void testRandom_withEqualWeights() throws Exception {
    LoadBalancer balancer = new LoadBalancer(null);
    Method method = LoadBalancer.class.getDeclaredMethod("selectRandom", List.class);
    method.setAccessible(true);

    ServiceInstance i1 = ServiceInstance.builder().id(1L).serviceId(1L).host("a").port(80).weight(100).healthy(true).enabled(true).build();
    ServiceInstance i2 = ServiceInstance.builder().id(2L).serviceId(1L).host("b").port(80).weight(100).healthy(true).enabled(true).build();
    List<ServiceInstance> instances = Arrays.asList(i1, i2);

    boolean i1Selected = false;
    boolean i2Selected = false;
    for (int i = 0; i < 100; i++) {
      ServiceInstance selected = (ServiceInstance) method.invoke(balancer, instances);
      if (selected == i1) i1Selected = true;
      if (selected == i2) i2Selected = true;
      if (i1Selected && i2Selected) break;
    }
    assertTrue(i1Selected, "i1 should be selected at least once");
    assertTrue(i2Selected, "i2 should be selected at least once");
  }

  @Test
  void testRandom_withZeroTotalWeight() throws Exception {
    LoadBalancer balancer = new LoadBalancer(null);
    Method method = LoadBalancer.class.getDeclaredMethod("selectRandom", List.class);
    method.setAccessible(true);

    ServiceInstance i1 = ServiceInstance.builder().id(1L).serviceId(1L).host("a").port(80).weight(0).healthy(true).enabled(true).build();
    ServiceInstance i2 = ServiceInstance.builder().id(2L).serviceId(1L).host("b").port(80).weight(0).healthy(true).enabled(true).build();
    List<ServiceInstance> instances = Arrays.asList(i1, i2);

    ServiceInstance selected = (ServiceInstance) method.invoke(balancer, instances);
    assertTrue(selected == i1 || selected == i2);
  }
}
