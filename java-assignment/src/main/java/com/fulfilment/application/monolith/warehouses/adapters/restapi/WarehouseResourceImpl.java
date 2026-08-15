package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.location.Location;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  private static final int MAX_WAREHOUSES_PER_LOCATION = 2;

  private final WarehouseRepository warehouseRepository;

  @Inject
  public WarehouseResourceImpl(WarehouseRepository warehouseRepository) {
    this.warehouseRepository = warehouseRepository;
  }

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  @Transactional
  public Warehouse createANewWarehouseUnit(@NotNull @Valid Warehouse data) {
    if (warehouseRepository.findByBusinessUnitCode(data.getBusinessUnitCode()) != null) {
      throw new WebApplicationException("Business unit code already exists", Response.Status.CONFLICT);
    }

    var location = validateAndResolveLocation(data.getLocation());
    var domain = toDomainWarehouse(data);
    validateCapacityAndStock(domain, location);
    validateWarehouseCreationFeasibility(location);

    domain.createdAt = LocalDateTime.now();
    warehouseRepository.create(domain);
    return toWarehouseResponse(domain);
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    var warehouse = warehouseRepository.findByBusinessUnitCode(id);
    if (warehouse == null) {
      throw notFound();
    }
    return toWarehouseResponse(warehouse);
  }

  @Override
  @Transactional
  public void archiveAWarehouseUnitByID(String id) {
    var warehouse = warehouseRepository.findByBusinessUnitCode(id);
    if (warehouse == null) {
      throw notFound();
    }
    warehouse.archivedAt = LocalDateTime.now();
    warehouseRepository.update(warehouse);
  }

  @Override
  @Transactional
  public Warehouse replaceTheCurrentActiveWarehouse(String businessUnitCode, @NotNull @Valid Warehouse data) {
    var current = warehouseRepository.findByBusinessUnitCode(businessUnitCode);
    if (current == null) {
      throw notFound();
    }

    var location = validateAndResolveLocation(data.getLocation());
    var replacement = toDomainWarehouse(data);
    replacement.businessUnitCode = businessUnitCode;
    validateCapacityAndStock(replacement, location);

    current.archivedAt = LocalDateTime.now();
    warehouseRepository.update(current);

    replacement.createdAt = LocalDateTime.now();
    warehouseRepository.create(replacement);
    return toWarehouseResponse(replacement);
  }

  private WebApplicationException notFound() {
    return new WebApplicationException("Warehouse unit not found", Response.Status.NOT_FOUND);
  }

  private Location validateAndResolveLocation(String locationIdentifier) {
    var location = resolveLocation(locationIdentifier);
    if (location == null) {
      throw new WebApplicationException("Location is invalid", Response.Status.BAD_REQUEST);
    }
    return location;
  }

  private void validateCapacityAndStock(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse, Location location) {
    if (warehouse.capacity == null || warehouse.stock == null) {
      throw new WebApplicationException("Warehouse capacity or stock is invalid", Response.Status.BAD_REQUEST);
    }
    if (warehouse.capacity > location.capacity()) {
      throw new WebApplicationException("Warehouse capacity exceeds location capacity", Response.Status.BAD_REQUEST);
    }
    if (warehouse.stock > warehouse.capacity) {
      throw new WebApplicationException("Warehouse stock exceeds warehouse capacity", Response.Status.BAD_REQUEST);
    }
  }

  private void validateWarehouseCreationFeasibility(Location location) {
    if (!canCreateWarehouseAtLocation(location)) {
      throw new WebApplicationException("Maximum number of warehouses reached for location", Response.Status.CONFLICT);
    }
  }

  private boolean canCreateWarehouseAtLocation(Location location) {
    return warehouseRepository.getAll().stream()
        .filter(existing -> location.identification().equals(existing.location))
        .filter(existing -> existing.archivedAt == null)
        .count()
        < MAX_WAREHOUSES_PER_LOCATION;
  }

  private Location resolveLocation(String locationIdentifier) {
    for (var location : Location.values()) {
      if (location.identification().equals(locationIdentifier)) {
        return location;
      }
    }
    return null;
  }

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomainWarehouse(
      Warehouse data) {
    var warehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    warehouse.businessUnitCode = data.getBusinessUnitCode();
    warehouse.location = data.getLocation();
    warehouse.capacity = data.getCapacity();
    warehouse.stock = data.getStock();
    return warehouse;
  }

  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }
}
