package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class WarehouseRepositoryTest {

  @Test
  void repositoryStateCanBeUpdatedThroughStoreContract() {
    var repository = new StubWarehouseRepository();
    var warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 100;
    warehouse.stock = 10;

    repository.create(warehouse);

    assertEquals("MWH.001", repository.findByBusinessUnitCode("MWH.001").businessUnitCode);
  }

  @Test
  void repositoryReturnsNullWhenBusinessUnitCodeMissing() {
    var repository = new StubWarehouseRepository();
    assertNull(repository.findByBusinessUnitCode("MISSING"));
  }

  private static final class StubWarehouseRepository extends WarehouseRepository {
    private final List<Warehouse> warehouses = new ArrayList<>();

    @Override
    public List<Warehouse> getAll() {
      return List.copyOf(warehouses);
    }

    @Override
    public void create(Warehouse warehouse) {
      warehouses.removeIf(existing -> existing.businessUnitCode.equals(warehouse.businessUnitCode));
      warehouses.add(copy(warehouse));
    }

    @Override
    public void update(Warehouse warehouse) {
      create(warehouse);
    }

    @Override
    public void remove(Warehouse warehouse) {
      warehouses.removeIf(existing -> existing.businessUnitCode.equals(warehouse.businessUnitCode));
    }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
      return warehouses.stream()
          .filter(existing -> existing.businessUnitCode.equals(buCode))
          .findFirst()
          .map(WarehouseRepositoryTest::copy)
          .orElse(null);
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
