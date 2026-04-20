package com.halfhex.fluffy.config;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisOptions;
import io.vertx.core.file.FileSystem;

import java.nio.charset.StandardCharsets;

public class ConfigVerticle extends AbstractVerticle {

  public static final String CONFIG_HOLDER_KEY = "config.holder";
  public static final String REDIS_CLIENT_KEY = "redis.client";

  private ConfigHolder configHolder;
  private Redis redisClient;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    FileSystem fs = vertx.fileSystem();
    fs.readFile("application.conf", ar -> {
      if (ar.succeeded()) {
        String content = ar.result().toString(StandardCharsets.UTF_8);
        JsonObject config = new JsonObject(content);
        ConfigHolder.init(config);
        this.configHolder = ConfigHolder.getInstance();

        String redisConnectionString = configHolder.getRedisHost() + ":" + configHolder.getRedisPort();
        RedisOptions redisOptions = new RedisOptions()
          .setConnectionString(redisConnectionString)
          .setMaxPoolSize(configHolder.getRedisMaxPoolSize());

        this.redisClient = Redis.createClient(vertx, redisOptions);

        vertx.sharedData().getAsyncMap("fluffy-shared", res -> {
          if (res.succeeded()) {
            res.result().put(CONFIG_HOLDER_KEY, configHolder, putAr -> {
              if (putAr.succeeded()) {
                res.result().put(REDIS_CLIENT_KEY, redisClient, putAr2 -> {
                  if (putAr2.succeeded()) {
                    System.out.println("ConfigVerticle started successfully");
                    startPromise.complete();
                  } else {
                    startPromise.fail(putAr2.cause());
                  }
                });
              } else {
                startPromise.fail(putAr.cause());
              }
            });
          } else {
            startPromise.fail(res.cause());
          }
        });
      } else {
        startPromise.fail("Failed to load application.conf: " + ar.cause().getMessage());
      }
    });
  }

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception {
    if (redisClient != null) {
      redisClient.close();
    }
    stopPromise.complete();
  }

  public ConfigHolder getConfigHolder() {
    return configHolder;
  }

  public Redis getRedisClient() {
    return redisClient;
  }
}
