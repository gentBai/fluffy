package com.halfhex.fluffy.gateway;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BackendClientTest {

  @Test
  void testHopByHopHeaders_containsExpectedHeaders() throws Exception {
    Field field = BackendClient.class.getDeclaredField("HOP_BY_HOP_HEADERS");
    field.setAccessible(true);
    @SuppressWarnings("unchecked")
    Set<String> headers = (Set<String>) field.get(null);

    assertTrue(headers.contains("connection"));
    assertTrue(headers.contains("keep-alive"));
    assertTrue(headers.contains("proxy-authenticate"));
    assertTrue(headers.contains("proxy-authorization"));
    assertTrue(headers.contains("te"));
    assertTrue(headers.contains("trailers"));
    assertTrue(headers.contains("transfer-encoding"));
    assertTrue(headers.contains("upgrade"));
    assertEquals(8, headers.size());
  }

  @Test
  void testHopByHopHeaders_isMutable() throws Exception {
    Field field = BackendClient.class.getDeclaredField("HOP_BY_HOP_HEADERS");
    field.setAccessible(true);
    @SuppressWarnings("unchecked")
    Set<String> headers = (Set<String>) field.get(null);

    int originalSize = headers.size();
    headers.add("x-custom-header");
    assertEquals(originalSize + 1, headers.size());
    headers.remove("x-custom-header");
  }
}