package com.halfhex.fluffy.config;

import io.vertx.core.json.JsonObject;

public class ConfigHolder {

  private static volatile ConfigHolder instance;
  private static final Object LOCK = new Object();
  private final JsonObject config;

  private ConfigHolder(JsonObject config) {
    this.config = config;
  }

  public static void init(JsonObject config) {
    synchronized (LOCK) {
      instance = new ConfigHolder(config);
    }
  }

  public static ConfigHolder getInstance() {
    if (instance == null) {
      throw new IllegalStateException("ConfigHolder not initialized");
    }
    return instance;
  }

  public static boolean isInitialized() {
    return instance != null;
  }

  public JsonObject getDbConfig() {
    return config.getJsonObject("db", new JsonObject());
  }

  public JsonObject getRedisConfig() {
    return config.getJsonObject("redis", new JsonObject());
  }

  public JsonObject getAppConfig() {
    return config.getJsonObject("app", new JsonObject());
  }

  public JsonObject getGatewayConfig() {
    return config.getJsonObject("gateway", new JsonObject());
  }

  public String getDbHost() {
    return getDbConfig().getString("host", "localhost");
  }

  public int getDbPort() {
    return getDbConfig().getInteger("port", 3306);
  }

  public String getDbDatabase() {
    return getDbConfig().getString("database", "fluffy");
  }

  public String getDbUsername() {
    return getDbConfig().getString("username", "root");
  }

  public String getDbPassword() {
    return getDbConfig().getString("password", "");
  }

  public int getDbMaxPoolSize() {
    return getDbConfig().getInteger("maxPoolSize", 10);
  }

  public long getDbConnectionTimeout() {
    return getDbConfig().getLong("connectionTimeout", 30000L);
  }

  public String getRedisHost() {
    return getRedisConfig().getString("host", "localhost");
  }

  public int getRedisPort() {
    return getRedisConfig().getInteger("port", 6379);
  }

  public int getRedisMaxPoolSize() {
    return getRedisConfig().getInteger("maxPoolSize", 10);
  }

  public long getRedisTimeout() {
    return getRedisConfig().getLong("timeout", 5000L);
  }

  public int getAppPort() {
    return getAppConfig().getInteger("port", 8888);
  }

  public int getAppAdminPort() {
    return getAppConfig().getInteger("adminPort", 8889);
  }

  public long getGatewayRequestTimeout() {
    return getGatewayConfig().getLong("requestTimeout", 30000L);
  }

  public int getGatewayMaxConcurrentRequests() {
    return getGatewayConfig().getInteger("maxConcurrentRequests", 10000);
  }

  public JsonObject getRawConfig() {
    return config;
  }
}
