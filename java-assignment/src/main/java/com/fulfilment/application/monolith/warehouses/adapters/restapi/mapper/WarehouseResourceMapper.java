package com.fulfilment.application.monolith.warehouses.adapters.restapi.mapper;

import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface WarehouseResourceMapper {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomain(com.warehouse.api.beans.Warehouse apiModel);
    com.warehouse.api.beans.Warehouse toApi(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainModel);
}
