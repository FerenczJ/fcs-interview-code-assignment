package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.location.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseUseCaseValidator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CreateWarehouseUseCaseTest {

  @Test
  void create_rejectsNullWarehouse() {
    var useCase = new CreateWarehouseUseCase(new StubWarehouseStore(), new StubWarehouseUseCaseValidator());

    var ex = assertThrows(IllegalArgumentException.class, () -> useCase.create(null));
    assertEquals("Warehouse data is invalid", ex.getMessage());
  }

  @Test
  void create_rejectsWarehouseWithNullBusinessUnitCode() {
    var useCase = new CreateWarehouseUseCase(new StubWarehouseStore(), new StubWarehouseUseCaseValidator());

    var warehouse = warehouse(Location.ZWOLLE_001.identification(), 40, 10);
    warehouse.businessUnitCode = null;

    var ex = assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    assertEquals("Warehouse data is invalid", ex.getMessage());
  }

  @Test
  void create_rejectsWarehouseWithNullLocation() {
    var useCase = new CreateWarehouseUseCase(new StubWarehouseStore(), new StubWarehouseUseCaseValidator());

    var warehouse = warehouse(Location.ZWOLLE_001.identification(), 40, 10);
    warehouse.location = null;

    var ex = assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    assertEquals("Warehouse data is invalid", ex.getMessage());
  }

  @Test
  void create_rejectsWarehouseWithNullCapacity() {
    var useCase = new CreateWarehouseUseCase(new StubWarehouseStore(), new StubWarehouseUseCaseValidator());

    var warehouse = warehouse(Location.ZWOLLE_001.identification(), 40, 10);
    warehouse.capacity = null;

    var ex = assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    assertEquals("Warehouse capacity or stock is invalid", ex.getMessage());
  }

  @Test
  void create_rejectsWarehouseWithNullStock() {
    var useCase = new CreateWarehouseUseCase(new StubWarehouseStore(), new StubWarehouseUseCaseValidator());

    var warehouse = warehouse(Location.ZWOLLE_001.identification(), 40, 10);
    warehouse.stock = null;

    var ex = assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    assertEquals("Warehouse capacity or stock is invalid", ex.getMessage());
  }

  @Test
  void create_rejectsInvalidLocation() {
    var useCase = new CreateWarehouseUseCase(new StubWarehouseStore(), new StubWarehouseUseCaseValidator());

    var warehouse = warehouse("NOT_A_REAL_LOCATION", 100, 10);

    var ex = assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    assertEquals("Location is invalid", ex.getMessage());
  }

  @Test
  void create_rejectsDuplicateBusinessUnitCode() {
    var store = new StubWarehouseStore();
    store.create(warehouseWithCode("MWH.001", Location.ZWOLLE_001.identification(), 40, 10));
    var useCase = new CreateWarehouseUseCase(store, new StubWarehouseUseCaseValidator(store, new StubLocationResolver()));

    var duplicateWarehouse = warehouseWithCode("MWH.001", Location.ZWOLLE_001.identification(), 40, 10);
    var ex = assertThrows(IllegalArgumentException.class, () -> useCase.create(duplicateWarehouse));

    assertEquals("Business unit code already exists", ex.getMessage());
  }

  @Test
  void create_acceptsValidLocation() {
    var useCase = new CreateWarehouseUseCase(new StubWarehouseStore(), new StubWarehouseUseCaseValidator());

    var warehouse = warehouse(Location.ZWOLLE_001.identification(), 40, 10);

    assertDoesNotThrow(() -> useCase.create(warehouse));
  }

  @Test
  void create_rejectsWhenMaximumNumberOfWarehousesForLocationIsReached() {
    var store = new StubWarehouseStore();
    store.create(activeWarehouse("MWH.001", Location.ZWOLLE_001.identification()));
    store.create(activeWarehouse("MWH.002", Location.ZWOLLE_001.identification()));
    var useCase = new CreateWarehouseUseCase(store, new StubWarehouseUseCaseValidator(store, new StubLocationResolver()));

    var warehouse = warehouseWithCode("MWH.003", Location.ZWOLLE_001.identification(), 40, 10);
    var ex = assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));

    assertEquals("Maximum number of warehouses reached for location", ex.getMessage());
  }

  @Test
  void create_ignoresArchivedWarehousesWhenCountingLocationCapacity() {
    var store = new StubWarehouseStore();
    store.create(archivedWarehouse(Location.ZWOLLE_001.identification()));
    var useCase = new CreateWarehouseUseCase(store, new StubWarehouseUseCaseValidator(store, new StubLocationResolver()));

    var warehouse = warehouseWithCode("MWH.002", Location.ZWOLLE_001.identification(), 40, 10);
    assertDoesNotThrow(() -> useCase.create(warehouse));
  }

  @Test
  void create_rejectsWhenWarehouseCapacityExceedsLocationCapacity() {
    var useCase = new CreateWarehouseUseCase(new StubWarehouseStore(), new StubWarehouseUseCaseValidator());

    var warehouse = warehouseWithCapacityAndStock("MWH.004", Location.ZWOLLE_001.identification(), 41, 10);
    var ex = assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));

    assertEquals("Warehouse capacity exceeds location capacity", ex.getMessage());
  }

  @Test
  void create_rejectsWhenStockExceedsWarehouseCapacity() {
    var useCase = new CreateWarehouseUseCase(new StubWarehouseStore(), new StubWarehouseUseCaseValidator());

    var warehouse = warehouseWithCapacityAndStock("MWH.005", Location.ZWOLLE_001.identification(), 40, 41);
    var ex = assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));

    assertEquals("Warehouse stock exceeds warehouse capacity", ex.getMessage());
  }

  @Test
  void create_acceptsCapacityAndStockAtLocationLimit() {
    var useCase = new CreateWarehouseUseCase(new StubWarehouseStore(), new StubWarehouseUseCaseValidator());

    var warehouse = warehouseWithCapacityAndStock("MWH.006", Location.ZWOLLE_001.identification(), 40, 40);
    assertDoesNotThrow(() -> useCase.create(warehouse));
  }

  private static Warehouse warehouse(String location, Integer capacity, Integer stock) {
    return warehouseWithCode("MWH.001", location, capacity, stock);
  }

  private static Warehouse warehouseWithCode(String code, String location, Integer capacity, Integer stock) {
    var warehouse = new Warehouse();
    warehouse.businessUnitCode = code;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    return warehouse;
  }

  private static Warehouse warehouseWithCapacityAndStock(String code, String location, Integer capacity, Integer stock) {
    return warehouseWithCode(code, location, capacity, stock);
  }

  private static Warehouse activeWarehouse(String code, String location) {
    var warehouse = warehouseWithCode(code, location, 40, 10);
    warehouse.createdAt = LocalDateTime.now();
    return warehouse;
  }

  private static Warehouse archivedWarehouse(String location) {
    var warehouse = activeWarehouse("MWH.001", location);
    warehouse.archivedAt = warehouse.createdAt.plusHours(1);
    return warehouse;
  }

  private static final class StubLocationResolver implements LocationResolver {
    @Override
    public Location resolveByIdentifier(String identifier) {
      for (var location : Location.values()) {
        if (location.identification().equals(identifier) || location.name().equals(identifier)) {
          return location;
        }
      }
      throw new IllegalArgumentException("No Location found with identification: " + identifier);
    }
  }

  private static final class StubWarehouseStore implements WarehouseRepository {
    private final List<Warehouse> warehouses = new ArrayList<>();

    @Override
    public java.util.List<Warehouse> getAll() {
      return List.copyOf(warehouses);
    }

    @Override
    public void create(Warehouse warehouse) {
      warehouses.removeIf(existing -> existing.businessUnitCode.equals(warehouse.businessUnitCode));
      warehouses.add(warehouse);
    }

    @Override
    public void update(Warehouse warehouse) {
      // intentionally empty: not used by these tests
    }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
      return warehouses.stream()
          .filter(existing -> existing.businessUnitCode.equals(buCode))
          .findFirst()
          .orElse(null);
    }

    @Override
    public long countByLocation(String location) {
      return warehouses.stream()
          .filter(existing -> existing.archivedAt == null)
          .filter(existing -> location.equals(existing.location))
          .count();
    }
  }

  private static final class StubWarehouseUseCaseValidator implements WarehouseUseCaseValidator {
    private final WarehouseRepository warehouseRepository;
    private final LocationResolver locationResolver;

    private StubWarehouseUseCaseValidator() {
      this(new StubWarehouseStore(), new StubLocationResolver());
    }

    private StubWarehouseUseCaseValidator(WarehouseRepository warehouseRepository, LocationResolver locationResolver) {
      this.warehouseRepository = warehouseRepository;
      this.locationResolver = locationResolver;
    }

    @Override
    public void validate(Warehouse warehouse) {
      if (warehouse == null || warehouse.businessUnitCode == null || warehouse.location == null) {
        throw new IllegalArgumentException("Warehouse data is invalid");
      }
      if (warehouse.capacity == null || warehouse.stock == null) {
        throw new IllegalArgumentException("Warehouse capacity or stock is invalid");
      }
      if (warehouseRepository.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
        throw new IllegalArgumentException("Business unit code already exists");
      }

      var location = resolveLocation(warehouse.location);
      if (warehouse.capacity > location.capacity()) {
        throw new IllegalArgumentException("Warehouse capacity exceeds location capacity");
      }
      if (warehouse.stock > warehouse.capacity) {
        throw new IllegalArgumentException("Warehouse stock exceeds warehouse capacity");
      }

      var existingWarehousesCount = warehouseRepository.countByLocation(location.identification());
      if (location.zone() <= existingWarehousesCount) {
        throw new IllegalArgumentException("Maximum number of warehouses reached for location");
      }
    }

    private Location resolveLocation(String identifier) {
      try {
        return locationResolver.resolveByIdentifier(identifier);
      } catch (Exception e) {
        throw new IllegalArgumentException("Location is invalid", e);
      }
    }
  }
}
