package com.halfhex.fluffy;

import com.halfhex.fluffy.config.ConfigVerticle;
import io.vertx.core.Vertx;

public class Application {
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new ConfigVerticle())
      .onSuccess(id -> {
        System.out.println("ConfigVerticle deployed: " + id);
        vertx.deployVerticle(new MainVerticle())
          .onSuccess(id2 -> System.out.println("MainVerticle deployed: " + id2))
          .onFailure(err -> System.err.println("Failed to deploy MainVerticle: " + err.getMessage()));
      })
      .onFailure(err -> {
        System.err.println("Failed to deploy ConfigVerticle: " + err.getMessage());
      });
  }
}