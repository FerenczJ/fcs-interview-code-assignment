package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseUseCaseValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {
  private final WarehouseRepository warehouseRepository;
  private final WarehouseUseCaseValidator warehouseUseCaseValidator;

  public CreateWarehouseUseCase(
      WarehouseRepository warehouseRepository,
      @Named("createWarehouseUseCaseValidator") WarehouseUseCaseValidator warehouseUseCaseValidator) {
    this.warehouseRepository = warehouseRepository;
    this.warehouseUseCaseValidator = warehouseUseCaseValidator;
  }

  @Override
  public void create(Warehouse warehouse) {
    warehouseUseCaseValidator.validate(warehouse);
    warehouseRepository.create(warehouse);
  }
}
