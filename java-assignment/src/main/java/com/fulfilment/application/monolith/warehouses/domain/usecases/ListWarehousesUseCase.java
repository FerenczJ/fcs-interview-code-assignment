package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ListWarehousesOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ListWarehousesUseCase implements ListWarehousesOperation {

    private final WarehouseRepository warehouseRepository;

    public ListWarehousesUseCase(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    public List<Warehouse> listWarehouses() {
        return warehouseRepository.getAll();
    }
}
