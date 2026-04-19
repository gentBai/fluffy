package com.halfhex.fluffy.security;

import io.vertx.core.Future;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimiterTest {

  @Mock
  private Redis redis;

  private RateLimiter rateLimiter;

  @BeforeEach
  void setUp() {
    rateLimiter = new RateLimiter(redis);
  }

  @Test
  void testRateLimiterConstructor() {
    assertNotNull(rateLimiter);
  }

  @Test
  void testRateLimitResult_allowed() {
    RateLimiter.RateLimitResult result = new RateLimiter.RateLimitResult(true, 99);
    assertTrue(result.isAllowed());
    assertEquals(99, result.getRemaining());
  }

  @Test
  void testRateLimitResult_notAllowed() {
    RateLimiter.RateLimitResult result = new RateLimiter.RateLimitResult(false, 0);
    assertFalse(result.isAllowed());
    assertEquals(0, result.getRemaining());
  }

  @Test
  void testRateLimitType_enumValues() {
    assertEquals(3, RateLimitType.values().length);
    assertNotNull(RateLimitType.IP);
    assertNotNull(RateLimitType.USERNAME);
    assertNotNull(RateLimitType.GLOBAL);
  }

  @Test
  void testRateLimitRule_constructorAndGetters() {
    RateLimitRule rule = new RateLimitRule(100, 5000, 100000, 10);
    assertEquals(100, rule.getRequestsPerMinute());
    assertEquals(5000, rule.getRequestsPerHour());
    assertEquals(100000, rule.getRequestsPerDay());
    assertEquals(10, rule.getBurstSize());
  }
}
