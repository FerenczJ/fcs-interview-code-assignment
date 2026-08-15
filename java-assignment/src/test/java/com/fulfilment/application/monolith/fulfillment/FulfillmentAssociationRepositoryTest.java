package com.fulfilment.application.monolith.fulfillment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class FulfillmentAssociationRepositoryTest {

  @Inject FulfillmentAssociationRepository repository;
  @Inject EntityManager entityManager;

  @BeforeEach
  @Transactional
  void resetTestData() {
    entityManager.createQuery("delete from FulfillmentAssociation").executeUpdate();
    entityManager.createNativeQuery("ALTER SEQUENCE fulfillment_association_seq RESTART WITH 1")
        .executeUpdate();

    persistAssociation(1L, 1L, "MWH.001");
    persistAssociation(1L, 2L, "MWH.001");
    persistAssociation(2L, 1L, "MWH.012");
    persistAssociation(3L, 3L, "MWH.023");

    entityManager.flush();
    entityManager.clear();
  }

  @Test
  void findByStoreReturnsAllAssociationsForStore() {
    var associations = repository.findByStore(1L);

    assertEquals(2, associations.size());
    assertAssociation(associations.get(0), 1L, 1L, "MWH.001");
    assertAssociation(associations.get(1), 1L, 2L, "MWH.001");
  }

  @Test
  void findByStoreReturnsEmptyListWhenStoreHasNoAssociations() {
    assertEquals(List.of(), repository.findByStore(999L));
  }

  @Test
  void findByStoreAndProductReturnsMatchingAssociation() {
    var associations = repository.findByStoreAndProduct(1L, 2L);

    assertEquals(1, associations.size());
    assertAssociation(associations.get(0), 1L, 2L, "MWH.001");
  }

  @Test
  void findByStoreAndProductReturnsEmptyListWhenCombinationIsMissing() {
    assertEquals(List.of(), repository.findByStoreAndProduct(1L, 3L));
  }

  @Test
  void findByWarehouseReturnsAllAssociationsForWarehouse() {
    var associations = repository.findByWarehouse("MWH.001");

    assertEquals(2, associations.size());
    assertAssociation(associations.get(0), 1L, 1L, "MWH.001");
    assertAssociation(associations.get(1), 1L, 2L, "MWH.001");
  }

  @Test
  void findByWarehouseReturnsEmptyListWhenWarehouseHasNoAssociations() {
    assertEquals(List.of(), repository.findByWarehouse("MWH.999"));
  }

  @Test
  void existsReturnsTrueOnlyForStoredCombination() {
    assertTrue(repository.exists(1L, 1L, "MWH.001"));
    assertFalse(repository.exists(1L, 1L, "MWH.012"));
    assertFalse(repository.exists(1L, 2L, "MWH.012"));
    assertFalse(repository.exists(2L, 3L, "MWH.012"));
  }

  private void persistAssociation(Long storeId, Long productId, String warehouseBusinessUnitCode) {
    entityManager.persist(new FulfillmentAssociation(storeId, productId, warehouseBusinessUnitCode));
  }

  private static void assertAssociation(
      FulfillmentAssociation association,
      Long storeId,
      Long productId,
      String warehouseBusinessUnitCode) {
    assertEquals(storeId, association.storeId);
    assertEquals(productId, association.productId);
    assertEquals(warehouseBusinessUnitCode, association.warehouseBusinessUnitCode);
  }
}
