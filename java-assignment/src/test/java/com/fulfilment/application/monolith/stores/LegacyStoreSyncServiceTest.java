package com.fulfilment.application.monolith.stores;

import static com.fulfilment.application.monolith.stores.StoreSyncEvent.SyncAction.CREATE;
import static com.fulfilment.application.monolith.stores.StoreSyncEvent.SyncAction.UPDATE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class LegacyStoreSyncServiceTest {

  @Test
  void onStoreTransactionCommit_handlesCreateEvent() throws Exception {
    var service = new LegacyStoreSyncService();
    var store = new Store("Demo Store");

    injectGateway(service, new LegacyStoreManagerGateway());

    store.id = 1L;

    assertDoesNotThrow(() -> service.onStoreTransactionCommit(new StoreSyncEvent(store, CREATE)));
  }

  @Test
  void onStoreTransactionCommit_handlesUpdateEvent() throws Exception {
    var service = new LegacyStoreSyncService();
    var store = new Store("Demo Store");

    injectGateway(service, new LegacyStoreManagerGateway());

    store.id = 2L;

    assertDoesNotThrow(() -> service.onStoreTransactionCommit(new StoreSyncEvent(store, UPDATE)));
  }

  private static void injectGateway(LegacyStoreSyncService service, LegacyStoreManagerGateway gateway) throws Exception {
    var field = LegacyStoreSyncService.class.getDeclaredField("legacyStoreManagerGateway");

    field.setAccessible(true);
    field.set(service, gateway);
  }
}
