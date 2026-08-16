package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.restapi.mapper.WarehouseResourceMapper;
import com.fulfilment.application.monolith.warehouses.domain.ports.*;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  private final ArchiveWarehouseOperation archiveWarehouseOperation;
  private final CreateWarehouseOperation createWarehouseOperation;
  private final ReplaceWarehouseOperation replaceWarehouseOperation;
  private final GetWarehouseOperation warehouseOperation;
  private final ListWarehousesOperation listWarehousesOperation;
  private final WarehouseResourceMapper mapper;

  @Inject
  public WarehouseResourceImpl(ArchiveWarehouseOperation archiveWarehouseUseCase,
                               CreateWarehouseOperation createWarehouseUseCase,
                               ReplaceWarehouseOperation replaceWarehouseUseCase,
                               GetWarehouseOperation getWarehouseOperation,
                               ListWarehousesOperation listWarehousesOperation,
                               WarehouseResourceMapper mapper) {
    this.archiveWarehouseOperation = archiveWarehouseUseCase;
    this.createWarehouseOperation = createWarehouseUseCase;
    this.replaceWarehouseOperation = replaceWarehouseUseCase;
    this.warehouseOperation = getWarehouseOperation;
    this.listWarehousesOperation = listWarehousesOperation;
    this.mapper = mapper;
  }

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return listWarehousesOperation.listWarehouses()
                .stream()
                .map(mapper::toApi)
                .toList();
  }

  @Override
  @Transactional
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    try {
      createWarehouseOperation.create(mapper.toDomain(data));
      return data;
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException("Cannot create warehouse: " + e.getMessage(), Response.Status.BAD_REQUEST);
    }
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    var warehouse = warehouseOperation.get(id);
    if (warehouse == null) throw notFound();

    return mapper.toApi(warehouse);
  }

  @Override
  @Transactional
  public void archiveAWarehouseUnitByID(String id) {
    try {
      archiveWarehouseOperation.archive(id);
    } catch (NotFoundException e) {
      throw new WebApplicationException("Cannot archive warehouse: " + e.getMessage(), Response.Status.NOT_FOUND);
    }
  }

  @Override
  @Transactional
  public Warehouse replaceTheCurrentActiveWarehouse(String businessUnitCode, @NotNull Warehouse data) {
    try {
      var replacement = mapper.toDomain(data);
      replacement.businessUnitCode = businessUnitCode;

      archiveWarehouseOperation.archive(businessUnitCode);
      replaceWarehouseOperation.replace(replacement);

      data.setBusinessUnitCode(businessUnitCode);
      return data;
    } catch (NotFoundException e) {
      throw new WebApplicationException("Cannot replace warehouse: " + e.getMessage(), Response.Status.NOT_FOUND);
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException("Cannot replace warehouse: " + e.getMessage(), Response.Status.BAD_REQUEST);
    }
  }

  private WebApplicationException notFound() {
    return new WebApplicationException("Warehouse unit not found", Response.Status.NOT_FOUND);
  }
}
