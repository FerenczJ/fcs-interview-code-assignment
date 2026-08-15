package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {
  private final WarehouseRepository warehouseRepository;

  public ArchiveWarehouseUseCase(WarehouseRepository warehouseRepository) {
    this.warehouseRepository = warehouseRepository;
  }

  @Override
  public void archive(String id) {
    var warehouse = warehouseRepository.findByBusinessUnitCode(id);
    if (warehouse == null) {
      throw new NotFoundException("Warehouse not found");
    }
    warehouse.archivedAt = LocalDateTime.now();
    warehouseRepository.update(warehouse);
  }
}
