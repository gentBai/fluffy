package com.halfhex.fluffy.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityEnumsTest {

  @Test
  void testTargetType_enumValues() {
    assertEquals(3, TargetType.values().length);
    assertEquals(TargetType.IP, TargetType.valueOf("IP"));
    assertEquals(TargetType.USER, TargetType.valueOf("USER"));
    assertEquals(TargetType.API_KEY, TargetType.valueOf("API_KEY"));
  }

  @Test
  void testSecurityCheckResult_enumValues() {
    assertEquals(3, SecurityCheckResult.values().length);
    assertEquals(SecurityCheckResult.ALLOWED, SecurityCheckResult.valueOf("ALLOWED"));
    assertEquals(SecurityCheckResult.BLOCKED, SecurityCheckResult.valueOf("BLOCKED"));
    assertEquals(SecurityCheckResult.BYPASSED, SecurityCheckResult.valueOf("BYPASSED"));
  }

  @Test
  void testRateLimitType_enumValues() {
    assertEquals(3, RateLimitType.values().length);
    assertEquals(RateLimitType.IP, RateLimitType.valueOf("IP"));
    assertEquals(RateLimitType.USERNAME, RateLimitType.valueOf("USERNAME"));
    assertEquals(RateLimitType.GLOBAL, RateLimitType.valueOf("GLOBAL"));
  }
}
