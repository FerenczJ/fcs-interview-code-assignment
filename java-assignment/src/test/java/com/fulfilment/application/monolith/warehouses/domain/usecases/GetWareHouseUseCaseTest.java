package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GetWareHouseUseCaseTest {

  @Test
  void get_returnsWarehouseFromRepository() {
    var repository = new StubWarehouseRepository();
    var warehouse = warehouse("MWH.001");
    repository.warehouse = warehouse;
    var useCase = new GetWareHouseUseCase(repository);

    var result = useCase.get("MWH.001");

    assertSame(warehouse, result);
    assertEquals("MWH.001", repository.lastRequestedBusinessUnitCode);
  }

  @Test
  void get_returnsNullWhenRepositoryHasNoWarehouse() {
    var useCase = new GetWareHouseUseCase(new StubWarehouseRepository());

    var result = useCase.get("MWH.404");

    assertNull(result);
  }

  private static Warehouse warehouse(String code) {
    var warehouse = new Warehouse();
    warehouse.businessUnitCode = code;
    return warehouse;
  }

  private static final class StubWarehouseRepository implements WarehouseRepository {
    private Warehouse warehouse;
    private String lastRequestedBusinessUnitCode;

    @Override
    public List<Warehouse> getAll() {
      return List.of();
    }

    @Override
    public void create(Warehouse warehouse) {
      // not used in these tests
    }

    @Override
    public void update(Warehouse warehouse) {
      // not used in these tests
    }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
      lastRequestedBusinessUnitCode = buCode;
      return warehouse;
    }

    @Override
    public long countByLocation(String location) {
      return 0;
    }
  }
}
