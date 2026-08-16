package com.fulfilment.application.monolith.warehouses.adapters.database;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;

@Mapper(componentModel = "cdi")
public interface WarehouseDatabaseMapper {
    Warehouse toDomain(DbWarehouse dbModel);

    @Mapping(target = "id", ignore = true)
    DbWarehouse toDb(Warehouse domainModel);

    @Mapping(target = "id", ignore = true)
    void updateDb(@MappingTarget DbWarehouse dbModel, Warehouse domainModel);
}
