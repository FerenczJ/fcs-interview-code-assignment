package com.fulfilment.application.monolith.warehouses.domain.validator;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseRepository;
import org.junit.jupiter.api.Test;

class CreateWarehouseUseCaseValidatorImplTest {

  @Test
  void validator_canBeConstructed_withRepositoryAndLocationResolver() {
    var validator = new CreateWarehouseUseCaseValidatorImpl(new StubRepository(), new StubLocationResolver());
    assertNotNull(validator);
  }

  private static final class StubRepository implements WarehouseRepository {
    @Override
    public java.util.List<com.fulfilment.application.monolith.warehouses.domain.models.Warehouse> getAll() {
      return java.util.List.of();
    }

    @Override
    public void create(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
      // intentionally left blank: test stub does not persist data
    }

    @Override
    public void update(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
      // intentionally left blank: not needed for validator instantiation test
    }

    @Override
    public com.fulfilment.application.monolith.warehouses.domain.models.Warehouse findByBusinessUnitCode(String buCode) {
      return null;
    }

    @Override
    public long countByLocation(String location) {
      return 0;
    }
  }

  private static final class StubLocationResolver implements LocationResolver {
    @Override
    public com.fulfilment.application.monolith.location.Location resolveByIdentifier(String identifier) {
      return com.fulfilment.application.monolith.location.Location.ZWOLLE_001;
    }
  }
}
