package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.location.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {
  private static final int MAX_WAREHOUSES_PER_LOCATION = 2;

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    validateBasicData(warehouse);

    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw new IllegalArgumentException("Business unit code already exists");
    }

    var location = validateAndResolveLocation(warehouse.location);
    validateCapacityAndStock(warehouse, location);
    validateWarehouseCreationFeasibility(location);

    warehouseStore.create(warehouse);
  }

  private void validateBasicData(Warehouse warehouse) {
    if (warehouse == null || warehouse.businessUnitCode == null || warehouse.location == null) {
      throw new IllegalArgumentException("Warehouse data is invalid");
    }
    if (warehouse.capacity == null || warehouse.stock == null) {
      throw new IllegalArgumentException("Warehouse capacity or stock is invalid");
    }
  }

  private Location validateAndResolveLocation(String identifier) {
    var location = locationResolver.resolveByIdentifier(identifier);
    if (location == null) {
      throw new IllegalArgumentException("Location is invalid");
    }
    return location;
  }

  private void validateCapacityAndStock(Warehouse warehouse, Location location) {
    if (warehouse.capacity > location.capacity()) {
      throw new IllegalArgumentException("Warehouse capacity exceeds location capacity");
    }
    if (warehouse.stock > warehouse.capacity) {
      throw new IllegalArgumentException("Warehouse stock exceeds warehouse capacity");
    }
  }

  private void validateWarehouseCreationFeasibility(Location location) {
    if (!canCreateWarehouseAtLocation(location)) {
      throw new IllegalArgumentException("Maximum number of warehouses reached for location");
    }
  }

  private boolean canCreateWarehouseAtLocation(Location location) {
    return warehouseStore.getAll().stream()
        .filter(existing -> location.identification().equals(existing.location))
        .filter(existing -> existing.archivedAt == null)
        .count()
        < MAX_WAREHOUSES_PER_LOCATION;
  }
}
