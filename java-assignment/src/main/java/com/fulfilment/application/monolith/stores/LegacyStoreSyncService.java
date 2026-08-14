package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class LegacyStoreSyncService {
    private static final Logger LOGGER = Logger.getLogger(LegacyStoreSyncService.class.getName());

    @Inject LegacyStoreManagerGateway legacyStoreManagerGateway;

    /**
     * This method automatically executes ONLY after the active database transaction
     * successfully finishes committing its data.
     */
    public void onStoreTransactionCommit(@Observes(during = TransactionPhase.AFTER_SUCCESS) StoreSyncEvent event) {
        var store = event.store();

        try {
            switch (event.action()) {
                case CREATE -> createStoreOnLegacySystem(store);
                case UPDATE -> updateStoreOnLegacySystem(store);
            }
        } catch (Exception e) {
            LOGGER.errorf(e, "Failed to synchronize store %d to legacy system after database commit.", store.id);
        }
    }

    private void createStoreOnLegacySystem(Store store) {
        try {
            LOGGER.infof("Transaction confirmed. Syncing new store creation downstream: %s (ID: %d)", store.name, store.id);
            legacyStoreManagerGateway.createStoreOnLegacySystem(store);
        } catch (Exception e) {
            LOGGER.errorf(e, "Failed to create store %s (ID: %d) in legacy system.", store.name, store.id);
        } finally {
            LOGGER.infof("Successful sync of new store creation downstream: %s (ID: %d)", store.name, store.id);
        }
    }

    private void updateStoreOnLegacySystem(Store store) {
        try {
            LOGGER.infof("Transaction confirmed. Syncing store updates downstream: %s (ID: %d)", store.name, store.id);
            legacyStoreManagerGateway.createStoreOnLegacySystem(store);
        } catch (Exception e) {
            LOGGER.errorf(e, "Failed to update store %s (ID: %d) in legacy system.", store.name, store.id);
        } finally {
            LOGGER.infof("Successful sync of update store downstream: %s (ID: %d)", store.name, store.id);
        }
    }
}
