package com.halfhex.fluffy;

import com.halfhex.fluffy.config.ConfigVerticle;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new ConfigVerticle())
      .onSuccess(id -> {
        logger.info("ConfigVerticle deployed: {}", id);
        vertx.deployVerticle(new MainVerticle())
          .onSuccess(id2 -> logger.info("MainVerticle deployed: {}", id2))
          .onFailure(err -> logger.error("Failed to deploy MainVerticle: {}", err.getMessage(), err));
      })
      .onFailure(err -> logger.error("Failed to deploy ConfigVerticle: {}", err.getMessage(), err));
  }
}
