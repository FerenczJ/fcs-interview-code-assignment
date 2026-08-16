package com.fulfilment.application.monolith.warehouses.domain.validator;

import com.fulfilment.application.monolith.location.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseUseCaseValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named("replaceWarehouseUseCaseValidator")
@ApplicationScoped
public class ReplaceWareHouseUseCaseValidatorImpl implements WarehouseUseCaseValidator {
  private final WarehouseRepository warehouseRepository;
  private final LocationResolver locationResolver;

  public ReplaceWareHouseUseCaseValidatorImpl(
      WarehouseRepository warehouseRepository, LocationResolver locationResolver) {
    this.warehouseRepository = warehouseRepository;
    this.locationResolver = locationResolver;
  }

  @Override
  public void validate(Warehouse warehouse) {
    validateBasicData(warehouse);

    var existingWarehouse = warehouseRepository.findByBusinessUnitCode(warehouse.businessUnitCode);
    validateStockMatches(existingWarehouse, warehouse);
    validateCapacityAccommodation(existingWarehouse, warehouse);

    var location = validateAndResolveLocation(warehouse.location);
    validateCapacityRules(warehouse, location);
  }

  private void validateBasicData(Warehouse warehouse) {
    if (warehouse == null || warehouse.businessUnitCode == null || warehouse.location == null) {
      throw new IllegalArgumentException("Warehouse data is invalid");
    }
    if (warehouse.capacity == null || warehouse.stock == null) {
      throw new IllegalArgumentException("Warehouse capacity or stock is invalid");
    }
  }

  private void validateStockMatches(Warehouse existingWarehouse, Warehouse warehouse) {
    if (existingWarehouse == null) {
      return;
    }
    if (existingWarehouse.stock != null && !existingWarehouse.stock.equals(warehouse.stock)) {
      throw new IllegalArgumentException("Warehouse stock must match the previous warehouse stock");
    }
  }

  private void validateCapacityAccommodation(Warehouse existingWarehouse, Warehouse warehouse) {
    if (existingWarehouse == null) {
      return;
    }
    if (warehouse.capacity < existingWarehouse.stock) {
      throw new IllegalArgumentException("New warehouse capacity cannot accommodate the previous stock");
    }
  }

  private Location validateAndResolveLocation(String identifier) {
    var location = locationResolver.resolveByIdentifier(identifier);
    if (location == null) {
      throw new IllegalArgumentException("Location is invalid");
    }
    return location;
  }

  private void validateCapacityRules(Warehouse warehouse, Location location) {
    if (warehouse.capacity > location.capacity()) {
      throw new IllegalArgumentException("Warehouse capacity exceeds location capacity");
    }
    if (warehouse.stock > warehouse.capacity) {
      throw new IllegalArgumentException("Warehouse stock exceeds warehouse capacity");
    }
  }
}
