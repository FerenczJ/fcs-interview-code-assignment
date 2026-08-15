package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ArchiveWarehouseUseCaseTest {

  @Test
  void archive_throwsNotFoundWhenWarehouseDoesNotExist() {
    var useCase = new ArchiveWarehouseUseCase(new StubWarehouseRepository());

    var ex = assertThrows(jakarta.ws.rs.NotFoundException.class, () -> useCase.archive("MWH.404"));

    assertEquals("Warehouse not found", ex.getMessage());
  }

  @Test
  void archive_setsArchivedAtAndUpdatesWarehouse() {
    var repository = new StubWarehouseRepository();
    repository.warehouse = warehouse("MWH.001");
    var useCase = new ArchiveWarehouseUseCase(repository);

    assertDoesNotThrow(() -> useCase.archive("MWH.001"));

    assertNotNull(repository.warehouse.archivedAt);
    assertEquals(repository.warehouse, repository.updatedWarehouse);
    assertEquals("MWH.001", repository.lastRequestedBusinessUnitCode);
  }

  private static Warehouse warehouse(String code) {
    var warehouse = new Warehouse();
    warehouse.businessUnitCode = code;
    warehouse.createdAt = LocalDateTime.now();
    return warehouse;
  }

  private static final class StubWarehouseRepository implements WarehouseRepository {
    private Warehouse warehouse;
    private Warehouse updatedWarehouse;
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
      updatedWarehouse = warehouse;
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
