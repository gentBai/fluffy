package com.halfhex.fluffy.config;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigHolderTest {

  @Test
  void testConfigHolder_fullConfig() {
    JsonObject config = createFullConfig();
    ConfigHolder holder = new ConfigHolder(config);

    assertEquals("localhost", holder.getDbHost());
    assertEquals(3306, holder.getDbPort());
    assertEquals("fluffy", holder.getDbDatabase());
    assertEquals("root", holder.getDbUsername());
    assertEquals("password", holder.getDbPassword());
    assertEquals(20, holder.getDbMaxPoolSize());
    assertEquals(30000L, holder.getDbConnectionTimeout());

    assertEquals("redis-host", holder.getRedisHost());
    assertEquals(6380, holder.getRedisPort());
    assertEquals(15, holder.getRedisMaxPoolSize());
    assertEquals(10000L, holder.getRedisTimeout());

    assertEquals(8888, holder.getAppPort());
    assertEquals(8889, holder.getAppAdminPort());

    assertEquals(60000L, holder.getGatewayRequestTimeout());
    assertEquals(5000, holder.getGatewayMaxConcurrentRequests());

    assertEquals(config, holder.getRawConfig());
  }

  @Test
  void testConfigHolder_defaults() {
    JsonObject emptyConfig = new JsonObject();
    ConfigHolder holder = new ConfigHolder(emptyConfig);

    assertEquals("localhost", holder.getDbHost());
    assertEquals(3306, holder.getDbPort());
    assertEquals("fluffy", holder.getDbDatabase());
    assertEquals("root", holder.getDbUsername());
    assertEquals("", holder.getDbPassword());
    assertEquals(10, holder.getDbMaxPoolSize());
    assertEquals(30000L, holder.getDbConnectionTimeout());

    assertEquals("localhost", holder.getRedisHost());
    assertEquals(6379, holder.getRedisPort());
    assertEquals(10, holder.getRedisMaxPoolSize());
    assertEquals(5000L, holder.getRedisTimeout());

    assertEquals(8888, holder.getAppPort());
    assertEquals(8889, holder.getAppAdminPort());

    assertEquals(30000L, holder.getGatewayRequestTimeout());
    assertEquals(10000, holder.getGatewayMaxConcurrentRequests());
  }

  @Test
  void testConfigHolder_getSubConfigs() {
    JsonObject config = createFullConfig();
    ConfigHolder holder = new ConfigHolder(config);

    assertNotNull(holder.getDbConfig());
    assertEquals("localhost", holder.getDbConfig().getString("host"));

    assertNotNull(holder.getRedisConfig());
    assertEquals("redis-host", holder.getRedisConfig().getString("host"));

    assertNotNull(holder.getAppConfig());
    assertEquals(8888, holder.getAppConfig().getInteger("port"));

    assertNotNull(holder.getGatewayConfig());
    assertEquals(60000L, holder.getGatewayConfig().getLong("requestTimeout"));
  }

  private JsonObject createFullConfig() {
    JsonObject dbConfig = new JsonObject()
      .put("host", "localhost")
      .put("port", 3306)
      .put("database", "fluffy")
      .put("username", "root")
      .put("password", "password")
      .put("maxPoolSize", 20)
      .put("connectionTimeout", 30000);

    JsonObject redisConfig = new JsonObject()
      .put("host", "redis-host")
      .put("port", 6380)
      .put("maxPoolSize", 15)
      .put("timeout", 10000);

    JsonObject appConfig = new JsonObject()
      .put("port", 8888)
      .put("adminPort", 8889);

    JsonObject gatewayConfig = new JsonObject()
      .put("requestTimeout", 60000)
      .put("maxConcurrentRequests", 5000);

    return new JsonObject()
      .put("db", dbConfig)
      .put("redis", redisConfig)
      .put("app", appConfig)
      .put("gateway", gatewayConfig);
  }
}
