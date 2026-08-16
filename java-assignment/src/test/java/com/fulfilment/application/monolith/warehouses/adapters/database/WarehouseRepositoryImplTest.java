package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class WarehouseRepositoryImplTest {

  @Inject WarehouseRepositoryImpl repository;
  @Inject WarehouseRepositoryImplTestTransactions transactions;
  @Inject EntityManager entityManager;

  @BeforeEach
  @Transactional
  void resetTestData() {
    entityManager.createQuery("delete from DbWarehouse").executeUpdate();
    entityManager.createNativeQuery("ALTER SEQUENCE warehouse_seq RESTART WITH 4").executeUpdate();
    entityManager.createNativeQuery("INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt) VALUES (1, 'MWH.001', 'ZWOLLE-001', 40, 10, '2024-07-01', null)").executeUpdate();
    entityManager.createNativeQuery("INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt) VALUES (2, 'MWH.012', 'AMSTERDAM-001', 50, 5, '2023-07-01', null)").executeUpdate();
    entityManager.createNativeQuery("INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt) VALUES (3, 'MWH.023', 'TILBURG-001', 30, 27, '2021-02-01', null)").executeUpdate();
    entityManager.flush();
    entityManager.clear();
  }

  @Test
  void getAllReturnsOnlyActiveWarehouses() {
    var all = repository.getAll();

    assertEquals(3, all.size());
    assertTrue(all.stream().allMatch(warehouse -> warehouse.archivedAt == null));
    assertEquals("MWH.001", all.get(0).businessUnitCode);
    assertEquals("MWH.012", all.get(1).businessUnitCode);
    assertEquals("MWH.023", all.get(2).businessUnitCode);
  }

  @Test
  void createPersistsWarehouseAndFindReturnsCopy() {
    transactions.createPersistsWarehouseAndFindReturnsCopy(repository);
  }

  @Test
  void updateReplacesExistingWarehouseState() {
    transactions.updateReplacesExistingWarehouseState(repository);
  }

  @Test
  void repositoryReturnsNullWhenBusinessUnitCodeMissing() {
    assertNull(repository.findByBusinessUnitCode("MISSING"));
  }

  @Test
  void countByLocationCountsOnlyActiveWarehouses() {
    assertEquals(1, repository.countByLocation("ZWOLLE-001"));
    assertEquals(1, repository.countByLocation("AMSTERDAM-001"));
    assertEquals(1, repository.countByLocation("TILBURG-001"));
    assertEquals(0, repository.countByLocation("MISSING"));
  }

  @ApplicationScoped
  static class WarehouseRepositoryImplTestTransactions {

    @Transactional
    void createPersistsWarehouseAndFindReturnsCopy(WarehouseRepositoryImpl repository) {
      var warehouse = warehouse("MWH.999", "ROTTERDAM-999", 123, 45, now(), null);

      repository.create(warehouse);

      var stored = repository.findByBusinessUnitCode("MWH.999");
      assertEquals("MWH.999", stored.businessUnitCode);
      assertEquals("ROTTERDAM-999", stored.location);
      assertEquals(123, stored.capacity);
      assertEquals(45, stored.stock);
      assertEquals(warehouse.createdAt, stored.createdAt);
      assertNull(stored.archivedAt);

      stored.location = "CHANGED";
      assertEquals("ROTTERDAM-999", repository.findByBusinessUnitCode("MWH.999").location);
    }

    @Transactional
    void updateReplacesExistingWarehouseState(WarehouseRepositoryImpl repository) {
      var warehouse = repository.findByBusinessUnitCode("MWH.001");
      warehouse.location = "AMSTERDAM-003";
      warehouse.capacity = 300;
      warehouse.stock = 40;
      warehouse.createdAt = now().plusDays(1);
      warehouse.archivedAt = now().plusHours(2);

      repository.update(warehouse);

      var stored = repository.findByBusinessUnitCode("MWH.001");
      assertEquals("AMSTERDAM-003", stored.location);
      assertEquals(300, stored.capacity);
      assertEquals(40, stored.stock);
      assertEquals(warehouse.createdAt, stored.createdAt);
      assertEquals(warehouse.archivedAt, stored.archivedAt);
    }
  }

  private static Warehouse warehouse(
      String businessUnitCode,
      String location,
      Integer capacity,
      Integer stock,
      LocalDateTime createdAt,
      LocalDateTime archivedAt) {
    var warehouse = new Warehouse();
    warehouse.businessUnitCode = businessUnitCode;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    warehouse.createdAt = createdAt;
    warehouse.archivedAt = archivedAt;
    return warehouse;
  }

  private static LocalDateTime now() {
    return LocalDateTime.of(2026, 8, 15, 12, 0);
  }
}
