package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ArchiveWarehouseUseCaseTest {

  @Test
  void archive_setsArchivedAtAndUpdatesWarehouse() {
    var store = new StubWarehouseStore();
    var useCase = new ArchiveWarehouseUseCase(store);
    var warehouse = warehouse("MWH.001", "ZWOLLE-001");

    useCase.archive(warehouse);

    var archivedWarehouse = store.findByBusinessUnitCode("MWH.001");
    assertNotNull(archivedWarehouse.archivedAt);
    assertTrue(archivedWarehouse.archivedAt.isAfter(archivedWarehouse.createdAt)
        || archivedWarehouse.archivedAt.isEqual(archivedWarehouse.createdAt));
  }

  @Test
  void archive_persistsUpdatedWarehouseState() {
    var store = new StubWarehouseStore();
    var useCase = new ArchiveWarehouseUseCase(store);
    var warehouse = warehouse("MWH.001", "ZWOLLE-001");

    assertDoesNotThrow(() -> useCase.archive(warehouse));
    assertEquals(1, store.getAll().size());
    assertEquals("MWH.001", store.getAll().get(0).businessUnitCode);
    assertNotNull(store.getAll().get(0).archivedAt);
  }

  private static Warehouse warehouse(String code, String location) {
    var warehouse = new Warehouse();
    warehouse.businessUnitCode = code;
    warehouse.location = location;
    warehouse.capacity = 40;
    warehouse.stock = 10;
    warehouse.createdAt = LocalDateTime.now();
    return warehouse;
  }

  private static final class StubWarehouseStore implements WarehouseStore {
    private final List<Warehouse> warehouses = new ArrayList<>();

    @Override
    public List<Warehouse> getAll() {
      return List.copyOf(warehouses);
    }

    @Override
    public void create(Warehouse warehouse) {
      persist(copy(warehouse));
    }

    @Override
    public void update(Warehouse warehouse) {
      delete(warehouse.businessUnitCode);
      persist(copy(warehouse));
    }

    @Override
    public void remove(Warehouse warehouse) {
      delete(warehouse.businessUnitCode);
    }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
      return warehouses.stream()
          .filter(existing -> existing.businessUnitCode.equals(buCode))
          .findFirst()
          .map(ArchiveWarehouseUseCaseTest::copy)
          .orElse(null);
    }

    private void persist(Warehouse warehouse) {
      warehouses.add(warehouse);
    }

    private void delete(String buCode) {
      warehouses.removeIf(existing -> existing.businessUnitCode.equals(buCode));
    }
  }

  private static Warehouse copy(Warehouse warehouse) {
    var copy = new Warehouse();
    copy.businessUnitCode = warehouse.businessUnitCode;
    copy.location = warehouse.location;
    copy.capacity = warehouse.capacity;
    copy.stock = warehouse.stock;
    copy.createdAt = warehouse.createdAt;
    copy.archivedAt = warehouse.archivedAt;
    return copy;
  }
}
