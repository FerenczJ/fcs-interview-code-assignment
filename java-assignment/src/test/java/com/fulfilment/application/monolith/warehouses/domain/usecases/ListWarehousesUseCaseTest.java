package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

class ListWarehousesUseCaseTest {

  @Test
  void listWarehouses_returnsWarehousesFromRepository() {
    var repository = new StubWarehouseRepository();
    var first = warehouse("MWH.001");
    var second = warehouse("MWH.002");
    repository.warehouses = List.of(first, second);
    var useCase = new ListWarehousesUseCase(repository);

    var result = useCase.listWarehouses();

    assertEquals(2, result.size());
    assertSame(first, result.get(0));
    assertSame(second, result.get(1));
  }

  @Test
  void listWarehouses_returnsEmptyListWhenRepositoryIsEmpty() {
    var useCase = new ListWarehousesUseCase(new StubWarehouseRepository());

    var result = useCase.listWarehouses();

    assertEquals(List.of(), result);
  }

  private static Warehouse warehouse(String code) {
    var warehouse = new Warehouse();
    warehouse.businessUnitCode = code;
    return warehouse;
  }

  private static final class StubWarehouseRepository implements WarehouseRepository {
    private List<Warehouse> warehouses = List.of();

    @Override
    public List<Warehouse> getAll() {
      return warehouses;
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
      return null;
    }

    @Override
    public long countByLocation(String location) {
      return 0;
    }
  }
}
