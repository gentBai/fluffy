package com.halfhex.fluffy;

import com.halfhex.fluffy.config.ConfigVerticle;
import io.vertx.core.Vertx;

public class Application {
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new ConfigVerticle(), res -> {
      if (res.succeeded()) {
        vertx.deployVerticle(new MainVerticle());
      } else {
        System.err.println("Failed to deploy ConfigVerticle: " + res.cause().getMessage());
      }
    });
  }
}
