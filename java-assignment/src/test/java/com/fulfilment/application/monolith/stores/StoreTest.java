package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class StoreTest {

  @Test
  void constructor_setsName_andLeavesStockAtDefault() {
    var store = new Store("Demo Store");

    assertNotNull(store);
    assertEquals("Demo Store", store.name);
    assertEquals(0, store.quantityProductsInStock);
  }
}
