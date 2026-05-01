package com.halfhex.fluffy.gateway;

import io.vertx.core.Vertx;
import io.vertx.sqlclient.Pool;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class RouteHandlerTest {

  @Test
  void testExactMatch() throws Exception {
    RouteHandler handler = new RouteHandler((Pool) null, Vertx.vertx());
    Method method = RouteHandler.class.getDeclaredMethod("pathMatches", String.class, String.class);
    method.setAccessible(true);

    assertTrue((Boolean) method.invoke(handler, "/api/users", "/api/users"));
    assertFalse((Boolean) method.invoke(handler, "/api/users", "/api/other"));
  }

  @Test
  void testDoubleWildcardMatch() throws Exception {
    RouteHandler handler = new RouteHandler((Pool) null, Vertx.vertx());
    Method method = RouteHandler.class.getDeclaredMethod("pathMatches", String.class, String.class);
    method.setAccessible(true);

    assertTrue((Boolean) method.invoke(handler, "/api/**", "/api/users"));
    assertTrue((Boolean) method.invoke(handler, "/api/**", "/api/users/1"));
    assertTrue((Boolean) method.invoke(handler, "/api/**", "/api"));
    assertFalse((Boolean) method.invoke(handler, "/api/**", "/other"));
  }

  @Test
  void testSingleWildcardMatch() throws Exception {
    RouteHandler handler = new RouteHandler((Pool) null, Vertx.vertx());
    Method method = RouteHandler.class.getDeclaredMethod("pathMatches", String.class, String.class);
    method.setAccessible(true);

    assertTrue((Boolean) method.invoke(handler, "/api/*", "/api/users"));
    assertTrue((Boolean) method.invoke(handler, "/api/*", "/api/anything"));
    assertFalse((Boolean) method.invoke(handler, "/api/*", "/api/users/1"));
  }

  @Test
  void testInlineWildcardMatch() throws Exception {
    RouteHandler handler = new RouteHandler((Pool) null, Vertx.vertx());
    Method method = RouteHandler.class.getDeclaredMethod("pathMatches", String.class, String.class);
    method.setAccessible(true);

    assertTrue((Boolean) method.invoke(handler, "/api/*/users", "/api/v1/users"));
    assertFalse((Boolean) method.invoke(handler, "/api/*/users", "/api/v1/other"));
  }

  @Test
  void testNullPatternOrPath() throws Exception {
    RouteHandler handler = new RouteHandler((Pool) null, Vertx.vertx());
    Method method = RouteHandler.class.getDeclaredMethod("pathMatches", String.class, String.class);
    method.setAccessible(true);

    assertFalse((Boolean) method.invoke(handler, null, "/api"));
    assertFalse((Boolean) method.invoke(handler, "/api", null));
  }
}
