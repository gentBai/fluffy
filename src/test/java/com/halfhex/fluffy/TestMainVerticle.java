package com.halfhex.fluffy;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {

  @Test
  void verticle_can_be_instantiated() {
    assertNotNull(new MainVerticle());
  }

  @Test
  void dummy_verticle_deploys(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new AbstractVerticle() {})
      .onSuccess(id -> testContext.completeNow())
      .onFailure(err -> testContext.failNow(err));
  }
}
