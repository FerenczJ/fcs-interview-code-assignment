package com.fulfilment.application.monolith.location;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocationGatewayTest {

  @ParameterizedTest
  @EnumSource(Location.class)
  void resolveByIdentifier_withEnumName_returnsLocationAndProperties(Location loc) {
    var gateway = new LocationGateway();
    var resolved = gateway.resolveByIdentifier(loc.name());

    // same instance returned and properties present
    assertSame(loc, resolved);
    assertEquals(loc.identification(), resolved.identification());
    assertTrue(resolved.identification().contains("-"));
    assertTrue(resolved.zone() > 0);
    assertTrue(resolved.capacity() > 0);
  }

  @Test
  void resolveByIdentifier_withInvalidName_throws() {
    var gateway = new LocationGateway();
    assertThrows(IllegalArgumentException.class, () -> gateway.resolveByIdentifier("UNKNOWN"));
  }
}