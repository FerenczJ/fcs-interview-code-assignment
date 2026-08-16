package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import jakarta.inject.Inject;

@ApplicationScoped
public class WarehouseRepositoryImpl implements WarehouseRepository, PanacheRepository<DbWarehouse> {

  private final WarehouseDatabaseMapper mapper;

  @Inject
  public WarehouseRepositoryImpl(WarehouseDatabaseMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public List<Warehouse> getAll() {
    return this.list("archivedAt is null")
            .stream()
            .map(mapper::toDomain).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    var dbWarehouse = mapper.toDb(warehouse);
    persist(dbWarehouse);
  }

  @Override
  public void update(Warehouse warehouse) {
    var dbWarehouse = findByBusinessUnitCodeEntity(warehouse.businessUnitCode);
    if (dbWarehouse == null) {
      return;
    }
    mapper.updateDb(dbWarehouse, warehouse);
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    var entity = findByBusinessUnitCodeEntity(buCode);
    return entity == null ? null : mapper.toDomain(entity);
  }

  @Override
  public long countByLocation(String location) {
    return count("archivedAt is null and location = ?1", location);
  }

  private DbWarehouse findByBusinessUnitCodeEntity(String buCode) {
    return find("businessUnitCode", buCode).firstResult();
  }
}
