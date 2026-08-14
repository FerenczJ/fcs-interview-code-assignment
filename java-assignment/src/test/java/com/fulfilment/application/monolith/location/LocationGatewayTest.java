package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class LocationGatewayTest {

  @ParameterizedTest
  @EnumSource(Location.class)
  void resolveByIdentifier_withEnumName_returnsLocationAndProperties(Location loc) {
    var gateway = new LocationGateway();
    var resolved = gateway.resolveByIdentifier(loc.name());

    assertSame(loc, resolved);
    assertEquals(loc.identification(), resolved.identification());
    assertTrue(resolved.identification().contains("-"));
    assertTrue(resolved.zone() > 0);
    assertTrue(resolved.capacity() > 0);
  }

  @Test
  void resolveByIdentifier_withInvalidName_returnsNull() {
    var gateway = new LocationGateway();
    assertNull(gateway.resolveByIdentifier("UNKNOWN"));
  }
}