package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.location.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {
  private final WarehouseStore warehouseStore;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    validateBasicData(newWarehouse);

    var existingWarehouse = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (existingWarehouse == null) {
      throw new IllegalArgumentException("Warehouse not found");
    }

    validateStockMatches(existingWarehouse, newWarehouse);
    var location = validateAndResolveLocation(newWarehouse.location);
    validateCapacityRules(newWarehouse, location);

    warehouseStore.update(newWarehouse);
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

  private Location validateAndResolveLocation(String identifier) {
    var location = resolveLocation(identifier);
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

  private Location resolveLocation(String identifier) {
    for (var location : Location.values()) {
      if (location.identification().equals(identifier)) {
        return location;
      }
    }
    return null;
  }
}
