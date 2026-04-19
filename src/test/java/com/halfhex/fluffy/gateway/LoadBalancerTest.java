package com.halfhex.fluffy.gateway;

import com.halfhex.fluffy.entity.GatewayRoute;
import com.halfhex.fluffy.entity.GatewayRoute.HttpMethod;
import com.halfhex.fluffy.repository.InstanceRepository;
import com.halfhex.fluffy.repository.InstanceRepository.Instance;
import com.halfhex.fluffy.repository.LoadBalanceRepository;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLPool;
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
class LoadBalancerTest {

  @Mock
  private MySQLPool mysqlPool;

  @Mock
  private InstanceRepository instanceRepository;

  @Mock
  private LoadBalanceRepository loadBalanceRepository;

  private LoadBalancer loadBalancer;

  @BeforeEach
  void setUp() {
    loadBalancer = new LoadBalancer(instanceRepository, loadBalanceRepository);
  }

  @Test
  void testSelectInstance_roundRobin() {
    Instance instance1 = createInstance(1L, 1L, "localhost", 8080, 100, true);
    Instance instance2 = createInstance(2L, 1L, "localhost", 8081, 100, true);
    List<Instance> instances = Arrays.asList(instance1, instance2);

    doAnswer(invocation -> {
      Promise<List<Instance>> p = invocation.getArgument(1);
      p.complete(instances);
      return null;
    }).when(instanceRepository).findHealthyByServiceId(any(), any());

    Future<Instance> future = loadBalancer.selectInstance(1L, "ROUND_ROBIN");
    assertNotNull(future);
  }

  @Test
  void testSelectInstance_random() {
    Instance instance1 = createInstance(1L, 1L, "localhost", 8080, 100, true);
    List<Instance> instances = Collections.singletonList(instance1);

    doAnswer(invocation -> {
      Promise<List<Instance>> p = invocation.getArgument(1);
      p.complete(instances);
      return null;
    }).when(instanceRepository).findHealthyByServiceId(any(), any());

    Future<Instance> future = loadBalancer.selectInstance(1L, "RANDOM");
    assertNotNull(future);
  }

  @Test
  void testSelectInstance_noHealthyInstances() {
    doAnswer(invocation -> {
      Promise<List<Instance>> p = invocation.getArgument(1);
      p.complete(Collections.emptyList());
      return null;
    }).when(instanceRepository).findHealthyByServiceId(any(), any());

    Future<Instance> future = loadBalancer.selectInstance(1L, "ROUND_ROBIN");
    assertNotNull(future);
  }

  @Test
  void testSelectInstance_defaultStrategy() {
    Instance instance = createInstance(1L, 1L, "localhost", 8080, 100, true);

    doAnswer(invocation -> {
      Promise<List<Instance>> p = invocation.getArgument(1);
      p.complete(Collections.singletonList(instance));
      return null;
    }).when(instanceRepository).findHealthyByServiceId(any(), any());

    Future<Instance> future = loadBalancer.selectInstance(1L);
    assertNotNull(future);
  }

  @Test
  void testRecordSuccess() {
    loadBalancer.recordSuccess(1L);
  }

  @Test
  void testRecordFailure() {
    loadBalancer.recordFailure(1L);
  }

  private Instance createInstance(Long id, Long serviceId, String host, int port, int weight, boolean healthy) {
    Instance instance = new Instance();
    instance.setId(id);
    instance.setServiceId(serviceId);
    instance.setHost(host);
    instance.setPort(port);
    instance.setWeight(weight);
    instance.setHealthy(healthy);
    instance.setCreatedAt(LocalDateTime.now());
    instance.setUpdatedAt(LocalDateTime.now());
    return instance;
  }
}