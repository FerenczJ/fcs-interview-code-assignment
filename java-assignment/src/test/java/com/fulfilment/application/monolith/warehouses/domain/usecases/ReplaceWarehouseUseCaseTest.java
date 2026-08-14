package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.location.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReplaceWarehouseUseCaseTest {

  @Test
  void replace_rejectsWhenWarehouseIsMissing() {
    var useCase = new ReplaceWarehouseUseCase(new StubWarehouseStore());

    var ex = assertThrows(IllegalArgumentException.class,
        () -> useCase.replace(warehouse("MWH.001", Location.ZWOLLE_001.identification(), 40, 10)));

    assertEquals("Warehouse not found", ex.getMessage());
  }

  @Test
  void replace_rejectsWhenWarehouseCapacityExceedsLocationCapacity() {
    var store = new StubWarehouseStore();
    store.create(warehouse("MWH.001", Location.ZWOLLE_001.identification(), 40, 10));
    var useCase = new ReplaceWarehouseUseCase(store);

    var warehouse = warehouse("MWH.001", Location.ZWOLLE_001.identification(), 41, 10);
    var ex = assertThrows(IllegalArgumentException.class, () -> useCase.replace(warehouse));

    assertEquals("Warehouse capacity exceeds location capacity", ex.getMessage());
  }

  @Test
  void replace_rejectsWhenStockExceedsWarehouseCapacity() {
    var store = new StubWarehouseStore();
    store.create(warehouse("MWH.001", Location.ZWOLLE_001.identification(), 39, 39));
    var useCase = new ReplaceWarehouseUseCase(store);

    var warehouse = warehouse("MWH.001", Location.ZWOLLE_001.identification(), 38, 39);
    var ex = assertThrows(IllegalArgumentException.class, () -> useCase.replace(warehouse));

    assertEquals("Warehouse stock exceeds warehouse capacity", ex.getMessage());
  }

  @Test
  void replace_rejectsWhenStockDoesNotMatchPreviousWarehouseStock() {
    var store = new StubWarehouseStore();
    store.create(warehouse("MWH.001", Location.ZWOLLE_001.identification(), 40, 10));
    var useCase = new ReplaceWarehouseUseCase(store);

    var warehouse = warehouse("MWH.001", Location.ZWOLLE_001.identification(), 40, 11);
    var ex = assertThrows(IllegalArgumentException.class, () -> useCase.replace(warehouse));

    assertEquals("Warehouse stock must match the previous warehouse stock", ex.getMessage());
  }

  @Test
  void replace_acceptsValidCapacityAndStock() {
    var store = new StubWarehouseStore();
    store.create(warehouse("MWH.001", Location.ZWOLLE_001.identification(), 40, 10));
    var useCase = new ReplaceWarehouseUseCase(store);

    var warehouse = warehouse("MWH.001", Location.ZWOLLE_001.identification(), 40, 10);

    assertDoesNotThrow(() -> useCase.replace(warehouse));
    assertEquals(1, store.getAll().size());
  }

  private static Warehouse warehouse(String code, String location, Integer capacity, Integer stock) {
    var warehouse = new Warehouse();
    warehouse.businessUnitCode = code;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
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
      warehouses.add(warehouse);
    }

    @Override
    public void update(Warehouse warehouse) {
      warehouses.removeIf(existing -> existing.businessUnitCode.equals(warehouse.businessUnitCode));
      warehouses.add(warehouse);
    }

    @Override
    public void remove(Warehouse warehouse) {
      warehouses.removeIf(existing -> existing.businessUnitCode.equals(warehouse.businessUnitCode));
    }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
      return warehouses.stream().filter(existing -> existing.businessUnitCode.equals(buCode)).findFirst().orElse(null);
    }
  }
}
