package com.halfhex.fluffy.gateway;

import com.halfhex.fluffy.entity.ServiceInstance;
import com.halfhex.fluffy.repository.ServiceInstanceRepository;
import com.halfhex.fluffy.repository.LoadBalanceStrategyRepository;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Pool;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class LoadBalancer {

  private static final String STRATEGY_ROUND_ROBIN = "ROUND_ROBIN";
  private static final String STRATEGY_RANDOM = "RANDOM";

  private final ServiceInstanceRepository instanceRepository;
  private final LoadBalanceStrategyRepository loadBalanceRepository;
  private final Map<Long, AtomicInteger> roundRobinCounters = new ConcurrentHashMap<>();
  private final Random random = new Random();

  public LoadBalancer(Pool mysqlPool) {
    this.instanceRepository = new ServiceInstanceRepository(mysqlPool);
    this.loadBalanceRepository = new LoadBalanceStrategyRepository(mysqlPool);
  }

  public LoadBalancer(ServiceInstanceRepository instanceRepository, LoadBalanceStrategyRepository loadBalanceRepository) {
    this.instanceRepository = instanceRepository;
    this.loadBalanceRepository = loadBalanceRepository;
  }

  public Future<ServiceInstance> selectInstance(Long serviceId, String strategyType) {
    String strategy = strategyType != null ? strategyType : STRATEGY_ROUND_ROBIN;

    Promise<List<ServiceInstance>> promise = Promise.promise();
    instanceRepository.findByServiceId(serviceId, promise);

    return promise.future().compose(instances -> {
      List<ServiceInstance> healthyInstances = instances.stream()
        .filter(i -> Boolean.TRUE.equals(i.getHealthy()))
        .filter(i -> Boolean.TRUE.equals(i.getEnabled()))
        .collect(Collectors.toList());

      if (healthyInstances.isEmpty()) {
        return Future.failedFuture("No healthy instances available for service: " + serviceId);
      }

      ServiceInstance selected;
      switch (strategy) {
        case STRATEGY_RANDOM:
          selected = selectRandom(healthyInstances);
          break;
        case STRATEGY_ROUND_ROBIN:
        default:
          selected = selectRoundRobin(serviceId, healthyInstances);
          break;
      }
      return Future.succeededFuture(selected);
    });
  }

  public Future<ServiceInstance> selectInstance(Long serviceId) {
    return selectInstance(serviceId, STRATEGY_ROUND_ROBIN);
  }

  private ServiceInstance selectRoundRobin(Long serviceId, List<ServiceInstance> instances) {
    AtomicInteger counter = roundRobinCounters.computeIfAbsent(serviceId, k -> new AtomicInteger(0));
    int totalWeight = instances.stream().mapToInt(ServiceInstance::getWeight).sum();
    if (totalWeight <= 0) {
      int index = Math.abs(counter.getAndIncrement()) % instances.size();
      return instances.get(index);
    }

    int weightPoint = Math.abs(counter.getAndIncrement()) % totalWeight;
    int cumulativeWeight = 0;
    for (ServiceInstance instance : instances) {
      cumulativeWeight += instance.getWeight();
      if (weightPoint < cumulativeWeight) {
        return instance;
      }
    }
    return instances.get(instances.size() - 1);
  }

  private ServiceInstance selectRandom(List<ServiceInstance> instances) {
    int totalWeight = instances.stream().mapToInt(ServiceInstance::getWeight).sum();
    if (totalWeight <= 0) {
      return instances.get(random.nextInt(instances.size()));
    }

    int randomWeight = random.nextInt(totalWeight);
    int cumulativeWeight = 0;
    for (ServiceInstance instance : instances) {
      cumulativeWeight += instance.getWeight();
      if (randomWeight < cumulativeWeight) {
        return instance;
      }
    }
    return instances.get(0);
  }

  public void recordSuccess(Long instanceId) {
  }

  public void recordFailure(Long instanceId) {
  }
}
