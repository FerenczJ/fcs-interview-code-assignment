package com.fulfilment.application.monolith.stores;

import static com.fulfilment.application.monolith.stores.StoreSyncEvent.SyncAction.CREATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class StoreSyncEventTest {

  @Test
  void recordKeepsStoreAndAction() {
    var store = new Store("Demo Store");
    var event = new StoreSyncEvent(store, CREATE);

    assertSame(store, event.store());
    assertEquals(CREATE, event.action());
  }
}
