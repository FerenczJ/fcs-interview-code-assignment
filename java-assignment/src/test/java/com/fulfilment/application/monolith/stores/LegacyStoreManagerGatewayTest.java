package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class LegacyStoreManagerGatewayTest {

  @Test
  void createStoreOnLegacySystem_doesNotThrow() {
    var gateway = new LegacyStoreManagerGateway();
    var store = new Store("Demo Store");

    store.quantityProductsInStock = 7;

    assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
  }

  @Test
  void updateStoreOnLegacySystem_doesNotThrow() {
    var gateway = new LegacyStoreManagerGateway();
    var store = new Store("Demo Store");

    store.quantityProductsInStock = 9;

    assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(store));
  }
}
