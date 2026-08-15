package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.location.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {
  private final WarehouseRepository warehouseRepository;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseRepository warehouseStore, LocationResolver locationResolver) {
    this.warehouseRepository = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    validateBasicData(warehouse);
    validateBusinessUnitCodeUniqueness(warehouse.businessUnitCode);

    var location = resolveLocation(warehouse.location);
    validateCapacityAndStock(warehouse, location);
    validateWarehouseCreationFeasibility(location);

    warehouseRepository.create(warehouse);
  }

  private void validateBasicData(Warehouse warehouse) {
    if (warehouse == null || warehouse.businessUnitCode == null || warehouse.location == null) {
      throw new IllegalArgumentException("Warehouse data is invalid");
    }
    if (warehouse.capacity == null || warehouse.stock == null) {
      throw new IllegalArgumentException("Warehouse capacity or stock is invalid");
    }
  }

  private Location resolveLocation(String identifier) {
    try {
      return locationResolver.resolveByIdentifier(identifier);
    } catch (Exception e) {
      throw new IllegalArgumentException("Location is invalid", e);
    }
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

    var existingWarehousesCount = warehouseRepository.countByLocation(location.identification());
    if (location.zone() <= existingWarehousesCount) {
      throw new IllegalArgumentException("Maximum number of warehouses reached for location");
    }
  }

  private void validateBusinessUnitCodeUniqueness(String businessUnitCode) {
    if (warehouseRepository.findByBusinessUnitCode(businessUnitCode) != null) {
      throw new IllegalArgumentException("Business unit code already exists");
    }
  }
}
