package com.halfhex.fluffy.security;

import io.vertx.core.Future;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.Request;
import io.vertx.redis.client.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimiterTest {

  @Mock
  private Redis redis;

  @Mock
  private Response mockResponse;

  @Mock
  private Response allowedResponse;

  @Mock
  private Response remainingResponse;

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

  @Test
  void testIsAllowed_redisFailure_returnsBlocked() throws Exception {
    when(redis.send(any(Request.class))).thenReturn(Future.failedFuture(new RuntimeException("Redis down")));

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<RateLimiter.RateLimitResult> ref = new AtomicReference<>();

    rateLimiter.isAllowed("test-key", 10, 60).onComplete(ar -> {
      ref.set(ar.result());
      latch.countDown();
    });

    assertTrue(latch.await(1, TimeUnit.SECONDS));
    assertNotNull(ref.get());
    assertFalse(ref.get().isAllowed());
    assertEquals(0, ref.get().getRemaining());
  }

  @Test
  void testIsAllowed_redisSuccess_returnsAllowed() throws Exception {
    when(allowedResponse.toInteger()).thenReturn(1);
    when(remainingResponse.toInteger()).thenReturn(5);
    when(mockResponse.get(0)).thenReturn(allowedResponse);
    when(mockResponse.get(1)).thenReturn(remainingResponse);
    when(redis.send(any(Request.class))).thenReturn(Future.succeededFuture(mockResponse));

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<RateLimiter.RateLimitResult> ref = new AtomicReference<>();

    rateLimiter.isAllowed("test-key", 10, 60).onComplete(ar -> {
      ref.set(ar.result());
      latch.countDown();
    });

    assertTrue(latch.await(1, TimeUnit.SECONDS));
    assertNotNull(ref.get());
    assertTrue(ref.get().isAllowed());
    assertEquals(5, ref.get().getRemaining());
  }

  @Test
  void testCheckRateLimit_skipsZeroWindows() throws Exception {
    RateLimitRule rule = new RateLimitRule(10, 0, 0, 10);

    when(allowedResponse.toInteger()).thenReturn(1);
    when(remainingResponse.toInteger()).thenReturn(5);
    when(mockResponse.get(0)).thenReturn(allowedResponse);
    when(mockResponse.get(1)).thenReturn(remainingResponse);
    when(redis.send(any(Request.class))).thenReturn(Future.succeededFuture(mockResponse));

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<RateLimiter.RateLimitResult> ref = new AtomicReference<>();

    rateLimiter.checkRateLimit(RateLimitType.IP, "127.0.0.1", rule).onComplete(ar -> {
      ref.set(ar.result());
      latch.countDown();
    });

    assertTrue(latch.await(1, TimeUnit.SECONDS));
    assertNotNull(ref.get());
    assertTrue(ref.get().isAllowed());

    // Should only call Redis once for the minute window, skipping hour and day
    verify(redis, times(1)).send(any(Request.class));
  }
}
