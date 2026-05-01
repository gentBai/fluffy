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
  void testHopByHopHeaders_isImmutable() throws Exception {
    Field field = BackendClient.class.getDeclaredField("HOP_BY_HOP_HEADERS");
    field.setAccessible(true);
    @SuppressWarnings("unchecked")
    Set<String> headers = (Set<String>) field.get(null);

    assertThrows(UnsupportedOperationException.class, () -> headers.add("x-custom-header"));
    assertThrows(UnsupportedOperationException.class, () -> headers.remove("connection"));
  }
}
