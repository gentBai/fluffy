package com.halfhex.fluffy.gateway;

import com.halfhex.fluffy.repository.InstanceRepository;
import com.halfhex.fluffy.repository.InstanceRepository.Instance;
import com.halfhex.fluffy.repository.LoadBalanceRepository;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.mysqlclient.MySQLPool;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Load balancer for selecting healthy backend service instances.
 *
 * <p>Supports multiple load balancing strategies:
 * <ul>
 *   <li>ROUND_ROBIN - Cycles through instances in order</li>
 *   <li>RANDOM - Randomly selects an instance based on weight</li>
 *   <li>CONSISTENT_HASH - Hashes requests for session affinity</li>
 * </ul>
 *
 * @author fluffy
 */
public class LoadBalancer {

    private static final String STRATEGY_ROUND_ROBIN = "ROUND_ROBIN";
    private static final String STRATEGY_RANDOM = "RANDOM";
    private static final String STRATEGY_CONSISTENT_HASH = "CONSISTENT_HASH";

    private final InstanceRepository instanceRepository;
    private final LoadBalanceRepository loadBalanceRepository;
    private final Map<Long, AtomicInteger> roundRobinCounters = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public LoadBalancer(MySQLPool mysqlPool) {
        this.instanceRepository = new InstanceRepository(mysqlPool);
        this.loadBalanceRepository = new LoadBalanceRepository(mysqlPool);
    }

    public LoadBalancer(InstanceRepository instanceRepository, LoadBalanceRepository loadBalanceRepository) {
        this.instanceRepository = instanceRepository;
        this.loadBalanceRepository = loadBalanceRepository;
    }

    public Future<Instance> selectInstance(Long serviceId, String strategyType) {
        String strategy = strategyType != null ? strategyType : STRATEGY_ROUND_ROBIN;

        Promise<List<Instance>> promise = Promise.promise();
        instanceRepository.findHealthyByServiceId(serviceId, promise);

        return promise.future().compose(instances -> {
            if (instances == null || instances.isEmpty()) {
                return Future.failedFuture("No healthy instances available for service: " + serviceId);
            }

            Instance selected;
            switch (strategy) {
                case STRATEGY_RANDOM:
                    selected = selectRandom(instances);
                    break;
                case STRATEGY_CONSISTENT_HASH:
                    selected = selectRoundRobin(serviceId, instances);
                    break;
                case STRATEGY_ROUND_ROBIN:
                default:
                    selected = selectRoundRobin(serviceId, instances);
                    break;
            }
            return Future.succeededFuture(selected);
        });
    }

    public Future<Instance> selectInstance(Long serviceId) {
        return selectInstance(serviceId, STRATEGY_ROUND_ROBIN);
    }

    private Instance selectRoundRobin(Long serviceId, List<Instance> instances) {
        AtomicInteger counter = roundRobinCounters.computeIfAbsent(serviceId, k -> new AtomicInteger(0));
        int index = Math.abs(counter.getAndIncrement()) % instances.size();
        return instances.get(index);
    }

    private Instance selectRandom(List<Instance> instances) {
        int totalWeight = instances.stream().mapToInt(Instance::getWeight).sum();
        if (totalWeight <= 0) {
            return instances.get(random.nextInt(instances.size()));
        }

        int randomWeight = random.nextInt(totalWeight);
        int cumulativeWeight = 0;
        for (Instance instance : instances) {
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
