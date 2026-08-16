package com.fulfilment.application.monolith.warehouses.adapters.restapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface WarehouseResourceMapper {
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomain(com.warehouse.api.beans.Warehouse apiModel);

    @Mapping(target = "id", ignore = true)
    com.warehouse.api.beans.Warehouse toApi(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainModel);
}
