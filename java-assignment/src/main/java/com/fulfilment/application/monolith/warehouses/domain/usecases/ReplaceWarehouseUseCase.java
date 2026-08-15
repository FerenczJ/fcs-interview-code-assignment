package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.location.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {
  private final WarehouseRepository warehouseRepository;
  private final LocationResolver locationResolver;

  public ReplaceWarehouseUseCase(WarehouseRepository warehouseRepository, LocationResolver locationResolver) {
    this.warehouseRepository = warehouseRepository;
    this.locationResolver = locationResolver;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    validateBasicData(newWarehouse);

    var existingWarehouse = warehouseRepository.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (existingWarehouse == null) {
      throw new NotFoundException("Warehouse not found");
    }

    validateStockMatches(existingWarehouse, newWarehouse);
    validateCapacityAccommodation(existingWarehouse, newWarehouse);

    var location = validateAndResolveLocation(newWarehouse.location);
    validateCapacityRules(newWarehouse, location);

    warehouseRepository.update(newWarehouse);
  }

  private void validateBasicData(Warehouse warehouse) {
    if (warehouse == null || warehouse.businessUnitCode == null || warehouse.location == null) {
      throw new IllegalArgumentException("Warehouse data is invalid");
    }
    if (warehouse.capacity == null || warehouse.stock == null) {
      throw new IllegalArgumentException("Warehouse capacity or stock is invalid");
    }
  }

  private void validateStockMatches(Warehouse existingWarehouse, Warehouse newWarehouse) {
    if (existingWarehouse.stock != null && !existingWarehouse.stock.equals(newWarehouse.stock)) {
      throw new IllegalArgumentException("Warehouse stock must match the previous warehouse stock");
    }
  }

  private void validateCapacityAccommodation(Warehouse existingWarehouse, Warehouse newWarehouse) {
    if (newWarehouse.capacity < existingWarehouse.stock) {
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
