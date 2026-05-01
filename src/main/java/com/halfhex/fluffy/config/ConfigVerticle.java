package com.halfhex.fluffy.config;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;

import java.nio.charset.StandardCharsets;

public class ConfigVerticle extends AbstractVerticle {

  public static final String CONFIG_HOLDER_KEY = "config.holder";
  public static final String REDIS_CLIENT_KEY = "redis.client";
  public static final String MYSQL_CLIENT_KEY = "mysql.client";

  private ConfigHolder configHolder;
  private Redis redisClient;
  private Pool mysqlPool;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.fileSystem().readFile("application.conf")
      .map(content -> content.toString(StandardCharsets.UTF_8))
      .map(JsonObject::new)
      .compose(config -> {
        ConfigHolder.init(config);
        this.configHolder = ConfigHolder.getInstance();

        String redisConnectionString = "redis://" + configHolder.getRedisHost() + ":" + configHolder.getRedisPort();
        RedisOptions redisOptions = new RedisOptions()
          .setConnectionString(redisConnectionString)
          .setMaxPoolSize(configHolder.getRedisMaxPoolSize());

        this.redisClient = Redis.createClient(vertx, redisOptions);

        MySQLConnectOptions mysqlConnectOptions = new MySQLConnectOptions()
          .setHost(configHolder.getDbHost())
          .setPort(configHolder.getDbPort())
          .setDatabase(configHolder.getDbDatabase())
          .setUser(configHolder.getDbUsername())
          .setPassword(configHolder.getDbPassword())
          .setCharset("utf8mb4");

        PoolOptions mysqlPoolOptions = new PoolOptions()
          .setMaxSize(configHolder.getDbMaxPoolSize());

        this.mysqlPool = Pool.pool(vertx, mysqlConnectOptions, mysqlPoolOptions);

        LocalMap<String, Object> localMap = vertx.sharedData().getLocalMap("fluffy.config");
        localMap.put(CONFIG_HOLDER_KEY, configHolder);
        localMap.put(REDIS_CLIENT_KEY, redisClient);
        localMap.put(MYSQL_CLIENT_KEY, mysqlPool);

        System.out.println("ConfigVerticle started successfully");
        startPromise.complete();
        return Future.succeededFuture();
      })
      .onFailure(startPromise::fail);
  }

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception {
    Future<Void> closeRedis = (redisClient != null) ? redisClient.close() : Future.succeededFuture();
    Future<Void> closeMysql = (mysqlPool != null) ? mysqlPool.close() : Future.succeededFuture();
    Future.all(closeRedis, closeMysql).onComplete(ar -> {
      if (ar.succeeded()) stopPromise.complete();
      else stopPromise.fail(ar.cause());
    });
  }

  public ConfigHolder getConfigHolder() {
    return configHolder;
  }

  public Redis getRedisClient() {
    return redisClient;
  }

  public Pool getMysqlPool() {
    return mysqlPool;
  }
}
