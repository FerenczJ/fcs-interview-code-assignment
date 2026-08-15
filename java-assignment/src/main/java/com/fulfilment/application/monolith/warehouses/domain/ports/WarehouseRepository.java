package com.fulfilment.application.monolith.warehouses.domain.ports;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import java.util.List;

public interface WarehouseRepository {

  List<Warehouse> getAll();

  void create(Warehouse warehouse);

  void update(Warehouse warehouse);

  Warehouse findByBusinessUnitCode(String buCode);

  long countByLocation(String location);
}
