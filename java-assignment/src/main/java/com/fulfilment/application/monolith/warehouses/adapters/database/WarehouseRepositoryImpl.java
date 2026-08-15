package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class WarehouseRepositoryImpl implements WarehouseRepository, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.list("archivedAt is null")
            .stream()
            .map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    var dbWarehouse = new DbWarehouse();
    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.createdAt = warehouse.createdAt;
    dbWarehouse.archivedAt = warehouse.archivedAt;
    persist(dbWarehouse);
  }

  @Override
  public void update(Warehouse warehouse) {
    var dbWarehouse = findByBusinessUnitCodeEntity(warehouse.businessUnitCode);
    if (dbWarehouse == null) {
      return;
    }
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.createdAt = warehouse.createdAt;
    dbWarehouse.archivedAt = warehouse.archivedAt;
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    var entity = findByBusinessUnitCodeEntity(buCode);
    return entity == null ? null : entity.toWarehouse();
  }

  @Override
  public long countByLocation(String location) {
    return count("archivedAt is null and location = ?1", location);
  }

  private DbWarehouse findByBusinessUnitCodeEntity(String buCode) {
    return find("businessUnitCode", buCode).firstResult();
  }
}
