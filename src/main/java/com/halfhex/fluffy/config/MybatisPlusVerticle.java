package com.halfhex.fluffy.config;

import com.halfhex.fluffy.mapper.*;
import io.vertx.core.*;

public class MybatisPlusVerticle extends AbstractVerticle {

    private MybatisPlusConfig mybatisPlusConfig;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        ConfigHolder configHolder = ConfigHolder.getInstance();
        String jdbcUrl = configHolder.getDatabaseUrl();
        String username = configHolder.getDatabaseUsername();
        String password = configHolder.getDatabasePassword();

        HikariCPConfig hikariConfig = new HikariCPConfig(jdbcUrl, username, password);
        javax.sql.DataSource dataSource = hikariConfig.getDataSource();

        try {
            mybatisPlusConfig = new MybatisPlusConfig(dataSource);
            startPromise.complete();
        } catch (Exception e) {
            startPromise.fail(e);
        }
    }

    public MybatisPlusConfig getMybatisPlusConfig() {
        return mybatisPlusConfig;
    }

    public <T> Future<T> executeBlocking(java.util.function.Supplier<T> supplier) {
        Promise<T> promise = Promise.promise();
        vertx.executeBlocking(future -> {
            try {
                future.complete(supplier.get());
            } catch (Exception e) {
                future.fail(e);
            }
        }, promise);
        return promise.future();
    }

    public GatewayRouteMapperWrapper createGatewayRouteMapperWrapper() {
        return new GatewayRouteMapperWrapper(this, mybatisPlusConfig.getGatewayRouteMapper());
    }

    public ServiceInstanceMapperWrapper createServiceInstanceMapperWrapper() {
        return new ServiceInstanceMapperWrapper(this, mybatisPlusConfig.getServiceInstanceMapper());
    }

    public BlacklistMapperWrapper createBlacklistMapperWrapper() {
        return new BlacklistMapperWrapper(this, mybatisPlusConfig.getBlacklistMapper());
    }

    public WhitelistMapperWrapper createWhitelistMapperWrapper() {
        return new WhitelistMapperWrapper(this, mybatisPlusConfig.getWhitelistMapper());
    }

    public ApiKeyMapperWrapper createApiKeyMapperWrapper() {
        return new ApiKeyMapperWrapper(this, mybatisPlusConfig.getApiKeyMapper());
    }

    public static class GatewayRouteMapperWrapper {
        private final MybatisPlusVerticle verticle;
        private final GatewayRouteMapper mapper;

        public GatewayRouteMapperWrapper(MybatisPlusVerticle verticle, GatewayRouteMapper mapper) {
            this.verticle = verticle;
            this.mapper = mapper;
        }

        public Future<com.halfhex.fluffy.entity.GatewayRoute> findById(Long id) {
            return verticle.executeBlocking(() -> mapper.selectById(id));
        }

        public Future<java.util.List<com.halfhex.fluffy.entity.GatewayRoute>> findAllEnabled() {
            return verticle.executeBlocking(() -> mapper.findAllEnabled());
        }

        public Future<com.halfhex.fluffy.entity.GatewayRoute> findByPathAndMethod(String path, String method) {
            return verticle.executeBlocking(() -> mapper.findByPathAndMethod(path, method));
        }

        public Future<Long> insert(com.halfhex.fluffy.entity.GatewayRoute route) {
            return verticle.executeBlocking(() -> {
                mapper.insert(route);
                return route.getId();
            });
        }

        public Future<Void> update(com.halfhex.fluffy.entity.GatewayRoute route) {
            return verticle.executeBlocking(() -> {
                mapper.updateById(route);
                return null;
            });
        }

        public Future<Void> deleteById(Long id) {
            return verticle.executeBlocking(() -> {
                mapper.deleteById(id);
                return null;
            });
        }
    }

    public static class ServiceInstanceMapperWrapper {
        private final MybatisPlusVerticle verticle;
        private final ServiceInstanceMapper mapper;

        public ServiceInstanceMapperWrapper(MybatisPlusVerticle verticle, ServiceInstanceMapper mapper) {
            this.verticle = verticle;
            this.mapper = mapper;
        }

        public Future<com.halfhex.fluffy.entity.ServiceInstance> findById(Long id) {
            return verticle.executeBlocking(() -> mapper.selectById(id));
        }

        public Future<java.util.List<com.halfhex.fluffy.entity.ServiceInstance>> findHealthyByServiceId(Long serviceId) {
            return verticle.executeBlocking(() -> mapper.findHealthyByServiceId(serviceId));
        }

        public Future<java.util.List<com.halfhex.fluffy.entity.ServiceInstance>> findByServiceId(Long serviceId) {
            return verticle.executeBlocking(() -> mapper.findByServiceId(serviceId));
        }

        public Future<Long> insert(com.halfhex.fluffy.entity.ServiceInstance instance) {
            return verticle.executeBlocking(() -> {
                mapper.insert(instance);
                return instance.getId();
            });
        }

        public Future<Void> update(com.halfhex.fluffy.entity.ServiceInstance instance) {
            return verticle.executeBlocking(() -> {
                mapper.updateById(instance);
                return null;
            });
        }
    }

    public static class BlacklistMapperWrapper {
        private final MybatisPlusVerticle verticle;
        private final BlacklistMapper mapper;

        public BlacklistMapperWrapper(MybatisPlusVerticle verticle, BlacklistMapper mapper) {
            this.verticle = verticle;
            this.mapper = mapper;
        }

        public Future<Boolean> checkIfBlacklisted(String targetType, String targetValue) {
            return verticle.executeBlocking(() -> mapper.checkIfBlacklisted(targetType, targetValue) != null);
        }

        public Future<Long> insert(com.halfhex.fluffy.entity.Blacklist blacklist) {
            return verticle.executeBlocking(() -> {
                mapper.insert(blacklist);
                return blacklist.getId();
            });
        }
    }

    public static class WhitelistMapperWrapper {
        private final MybatisPlusVerticle verticle;
        private final WhitelistMapper mapper;

        public WhitelistMapperWrapper(MybatisPlusVerticle verticle, WhitelistMapper mapper) {
            this.verticle = verticle;
            this.mapper = mapper;
        }

        public Future<Boolean> checkIfWhitelisted(String targetType, String targetValue) {
            return verticle.executeBlocking(() -> mapper.checkIfWhitelisted(targetType, targetValue) != null);
        }

        public Future<Long> insert(com.halfhex.fluffy.entity.Whitelist whitelist) {
            return verticle.executeBlocking(() -> {
                mapper.insert(whitelist);
                return whitelist.getId();
            });
        }
    }

    public static class ApiKeyMapperWrapper {
        private final MybatisPlusVerticle verticle;
        private final ApiKeyMapper mapper;

        public ApiKeyMapperWrapper(MybatisPlusVerticle verticle, ApiKeyMapper mapper) {
            this.verticle = verticle;
            this.mapper = mapper;
        }

        public Future<com.halfhex.fluffy.entity.ApiKey> findByKeyValue(String keyValue) {
            return verticle.executeBlocking(() -> mapper.findByKeyValue(keyValue));
        }

        public Future<Long> insert(com.halfhex.fluffy.entity.ApiKey apiKey) {
            return verticle.executeBlocking(() -> {
                mapper.insert(apiKey);
                return apiKey.getId();
            });
        }
    }
}