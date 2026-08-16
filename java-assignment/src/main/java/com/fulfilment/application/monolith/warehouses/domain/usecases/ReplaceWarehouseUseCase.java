package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseUseCaseValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {
  private final WarehouseRepository warehouseRepository;
  private final WarehouseUseCaseValidator warehouseUseCaseValidator;

  public ReplaceWarehouseUseCase(
      WarehouseRepository warehouseRepository,
      @Named("replaceWarehouseUseCaseValidator") WarehouseUseCaseValidator warehouseUseCaseValidator) {
    this.warehouseRepository = warehouseRepository;
    this.warehouseUseCaseValidator = warehouseUseCaseValidator;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    warehouseUseCaseValidator.validate(newWarehouse);

    var existingWarehouse = warehouseRepository.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (existingWarehouse == null) {
      throw new NotFoundException("Warehouse not found");
    }

    warehouseRepository.update(newWarehouse);
  }
}
