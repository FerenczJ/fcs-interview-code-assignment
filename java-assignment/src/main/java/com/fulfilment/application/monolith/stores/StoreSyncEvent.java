package com.fulfilment.application.monolith.stores;

/**
 * Event payload representing a verified change to a Store entity.
 */
public record StoreSyncEvent(Store store, SyncAction action) {
    public enum SyncAction {
        CREATE,
        UPDATE
    }
}