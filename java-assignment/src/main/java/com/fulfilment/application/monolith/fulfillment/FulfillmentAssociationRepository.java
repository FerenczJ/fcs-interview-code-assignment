package com.fulfilment.application.monolith.fulfillment;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class FulfillmentAssociationRepository implements PanacheRepository<FulfillmentAssociation> {

  public List<FulfillmentAssociation> findByStore(Long storeId) {
    return list("storeId", storeId);
  }

  public List<FulfillmentAssociation> findByStoreAndProduct(Long storeId, Long productId) {
    return list("storeId = ?1 and productId = ?2", storeId, productId);
  }

  public List<FulfillmentAssociation> findByWarehouse(String warehouseBusinessUnitCode) {
    return list("warehouseBusinessUnitCode", warehouseBusinessUnitCode);
  }

  public boolean exists(Long storeId, Long productId, String warehouseBusinessUnitCode) {
    return count("storeId = ?1 and productId = ?2 and warehouseBusinessUnitCode = ?3",
        storeId, productId, warehouseBusinessUnitCode) > 0;
  }
}
