package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.GetWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GetWareHouseUseCase implements GetWarehouseOperation {

    private final WarehouseRepository warehouseRepository;

    public GetWareHouseUseCase(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    public Warehouse get(String businessUnitCode) {
        return warehouseRepository.findByBusinessUnitCode(businessUnitCode);
    }
}
