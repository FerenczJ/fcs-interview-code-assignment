package com.fulfilment.application.monolith.warehouses.domain.ports;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;

public interface WarehouseUseCaseValidator {
  void validate(Warehouse warehouse);
}
