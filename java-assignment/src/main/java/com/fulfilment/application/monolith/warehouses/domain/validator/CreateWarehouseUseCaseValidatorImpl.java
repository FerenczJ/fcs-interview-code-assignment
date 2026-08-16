package com.fulfilment.application.monolith.warehouses.domain.validator;

import com.fulfilment.application.monolith.location.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseUseCaseValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named("createWarehouseUseCaseValidator")
@ApplicationScoped
public class CreateWarehouseUseCaseValidatorImpl implements WarehouseUseCaseValidator {
  private final WarehouseRepository warehouseRepository;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCaseValidatorImpl(
      WarehouseRepository warehouseRepository, LocationResolver locationResolver) {
    this.warehouseRepository = warehouseRepository;
    this.locationResolver = locationResolver;
  }

  @Override
  public void validate(Warehouse warehouse) {
    validateBasicData(warehouse);
    validateBusinessUnitCodeUniqueness(warehouse.businessUnitCode);

    var location = resolveLocation(warehouse.location);
    validateCapacityAndStock(warehouse, location);
    validateWarehouseCreationFeasibility(location);
  }

  private void validateBasicData(Warehouse warehouse) {
    if (warehouse == null || warehouse.businessUnitCode == null || warehouse.location == null) {
      throw new IllegalArgumentException("Warehouse data is invalid");
    }
    if (warehouse.capacity == null || warehouse.stock == null) {
      throw new IllegalArgumentException("Warehouse capacity or stock is invalid");
    }
  }

  private void validateBusinessUnitCodeUniqueness(String businessUnitCode) {
    if (warehouseRepository.findByBusinessUnitCode(businessUnitCode) != null) {
      throw new IllegalArgumentException("Business unit code already exists");
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
}
