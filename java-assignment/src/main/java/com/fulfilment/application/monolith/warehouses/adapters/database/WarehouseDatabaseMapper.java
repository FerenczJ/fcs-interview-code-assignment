package com.fulfilment.application.monolith.warehouses.adapters.database;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;

@Mapper(componentModel = "cdi")
public interface WarehouseDatabaseMapper {
    Warehouse toDomain(DbWarehouse dbModel);
    DbWarehouse toDb(Warehouse domainModel);
    void updateDb(@MappingTarget DbWarehouse dbModel, Warehouse domainModel);
}
