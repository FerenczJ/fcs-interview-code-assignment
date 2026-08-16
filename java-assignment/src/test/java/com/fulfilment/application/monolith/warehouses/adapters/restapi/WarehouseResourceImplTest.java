package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.adapters.restapi.mapper.WarehouseResourceMapper;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.GetWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ListWarehousesOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class WarehouseResourceImplTest {

  private final WarehouseResourceMapper mapper = Mappers.getMapper(WarehouseResourceMapper.class);

  @Test
  void listAllWarehousesUnits_mapsDomainWarehousesToApiWarehouses() {
    var warehouse1 = warehouse("MWH.001", "London", 100, 25);
    var warehouse2 = warehouse("MWH.002", "Berlin", 200, 75);
    var listOperation = new RecordingListWarehousesOperation(List.of(warehouse1, warehouse2));
    var resource = new WarehouseResourceImpl(
        new NoopArchiveWarehouseUseCase(),
        new NoopCreateWarehouseUseCase(),
        new NoopReplaceWarehouseUseCase(),
        new NoopGetWarehouseOperation(null),
        listOperation,
        mapper);

    var result = resource.listAllWarehousesUnits();

    assertEquals(2, result.size());
    assertEquals("MWH.001", result.get(0).getBusinessUnitCode());
    assertEquals("London", result.get(0).getLocation());
    assertEquals(100, result.get(0).getCapacity());
    assertEquals(25, result.get(0).getStock());
    assertEquals("MWH.002", result.get(1).getBusinessUnitCode());
    assertEquals("Berlin", result.get(1).getLocation());
    assertEquals(200, result.get(1).getCapacity());
    assertEquals(75, result.get(1).getStock());
    assertEquals(1, listOperation.calls);
  }

  @Test
  void createANewWarehouseUnit_delegatesToCreateOperation_andReturnsInput() {
    var apiWarehouse = apiWarehouse("MWH.003", "Paris", 150, 60);
    var createOperation = new RecordingCreateWarehouseUseCase();
    var resource = new WarehouseResourceImpl(
        new NoopArchiveWarehouseUseCase(),
        createOperation,
        new NoopReplaceWarehouseUseCase(),
        new NoopGetWarehouseOperation(null),
        new RecordingListWarehousesOperation(List.of()),
        mapper);

    var result = resource.createANewWarehouseUnit(apiWarehouse);

    assertSame(apiWarehouse, result);
    assertEquals(1, createOperation.calls);
    assertEquals("MWH.003", createOperation.lastWarehouse.businessUnitCode);
    assertEquals("Paris", createOperation.lastWarehouse.location);
    assertEquals(150, createOperation.lastWarehouse.capacity);
    assertEquals(60, createOperation.lastWarehouse.stock);
  }

  @Test
  void createANewWarehouseUnit_mapsIllegalArgumentExceptionToBadRequest() {
    var resource = new WarehouseResourceImpl(
        new NoopArchiveWarehouseUseCase(),
        new RecordingCreateWarehouseUseCase(new IllegalArgumentException("Warehouse data is invalid")),
        new NoopReplaceWarehouseUseCase(),
        new NoopGetWarehouseOperation(null),
        new RecordingListWarehousesOperation(List.of()),
        mapper);

    var ex = assertThrows(WebApplicationException.class,
        () -> resource.createANewWarehouseUnit(invalidCreatePayload()));

    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
    assertEquals("Cannot create warehouse: Warehouse data is invalid", ex.getMessage());
  }

  @Test
  void getAWarehouseUnitByID_returnsMappedApiWarehouse_whenFound() {
    var domainWarehouse = warehouse("MWH.004", "Madrid", 90, 12);
    var getOperation = new RecordingGetWarehouseOperation(domainWarehouse);
    var resource = new WarehouseResourceImpl(
        new NoopArchiveWarehouseUseCase(),
        new NoopCreateWarehouseUseCase(),
        new NoopReplaceWarehouseUseCase(),
        getOperation,
        new RecordingListWarehousesOperation(List.of()),
        mapper);

    var result = resource.getAWarehouseUnitByID("MWH.004");

    assertEquals("MWH.004", result.getBusinessUnitCode());
    assertEquals("Madrid", result.getLocation());
    assertEquals(90, result.getCapacity());
    assertEquals(12, result.getStock());
    assertEquals("MWH.004", getOperation.lastRequestedId);
  }

  @Test
  void getAWarehouseUnitByID_throwsNotFoundWhenWarehouseIsMissing() {
    var resource = new WarehouseResourceImpl(
        new NoopArchiveWarehouseUseCase(),
        new NoopCreateWarehouseUseCase(),
        new NoopReplaceWarehouseUseCase(),
        new NoopGetWarehouseOperation(null),
        new RecordingListWarehousesOperation(List.of()),
        mapper);

    var ex = assertThrows(WebApplicationException.class, () -> resource.getAWarehouseUnitByID("missing"));

    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), ex.getResponse().getStatus());
    assertEquals("Warehouse unit not found", ex.getMessage());
  }

  @Test
  void archiveAWarehouseUnitByID_delegatesToArchiveOperation() {
    var archiveOperation = new RecordingArchiveWarehouseUseCase();
    var resource = new WarehouseResourceImpl(
        archiveOperation,
        new NoopCreateWarehouseUseCase(),
        new NoopReplaceWarehouseUseCase(),
        new NoopGetWarehouseOperation(null),
        new RecordingListWarehousesOperation(List.of()),
        mapper);

    resource.archiveAWarehouseUnitByID("MWH.005");

    assertEquals(1, archiveOperation.calls);
    assertEquals("MWH.005", archiveOperation.lastArchivedId);
  }

  @Test
  void archiveAWarehouseUnitByID_mapsNotFoundExceptionToNotFoundStatus() {
    var archiveOperation = new RecordingArchiveWarehouseUseCase(new NotFoundException("Warehouse not found"));
    var resource = new WarehouseResourceImpl(
        archiveOperation,
        new NoopCreateWarehouseUseCase(),
        new NoopReplaceWarehouseUseCase(),
        new NoopGetWarehouseOperation(null),
        new RecordingListWarehousesOperation(List.of()),
        mapper);

    var ex = assertThrows(WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("MWH.005"));

    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), ex.getResponse().getStatus());
    assertEquals("Cannot archive warehouse: Warehouse not found", ex.getMessage());
  }

  @Test
  void replaceTheCurrentActiveWarehouse_archivesAndReplaces_usingPathBusinessUnitCode() {
    var archiveOperation = new RecordingArchiveWarehouseUseCase();
    var replaceOperation = new RecordingReplaceWarehouseUseCase();
    var resource = new WarehouseResourceImpl(
        archiveOperation,
        new NoopCreateWarehouseUseCase(),
        replaceOperation,
        new NoopGetWarehouseOperation(null),
        new RecordingListWarehousesOperation(List.of()),
        mapper);
    var payload = apiWarehouse("IGNORED", "Rome", 120, 40);

    var result = resource.replaceTheCurrentActiveWarehouse("MWH.006", payload);

    assertSame(payload, result);
    assertEquals(1, archiveOperation.calls);
    assertEquals("MWH.006", archiveOperation.lastArchivedId);
    assertEquals(1, replaceOperation.calls);
    assertEquals("MWH.006", replaceOperation.lastWarehouse.businessUnitCode);
    assertEquals("Rome", replaceOperation.lastWarehouse.location);
    assertEquals(120, replaceOperation.lastWarehouse.capacity);
    assertEquals(40, replaceOperation.lastWarehouse.stock);
  }

  @Test
  void replaceTheCurrentActiveWarehouse_mapsNotFoundExceptionToNotFoundStatus() {
    var archiveOperation = new RecordingArchiveWarehouseUseCase(new NotFoundException("Warehouse not found"));
    var resource = new WarehouseResourceImpl(
        archiveOperation,
        new NoopCreateWarehouseUseCase(),
        new RecordingReplaceWarehouseUseCase(),
        new NoopGetWarehouseOperation(null),
        new RecordingListWarehousesOperation(List.of()),
        mapper);

    var ex = assertThrows(WebApplicationException.class,
        () -> resource.replaceTheCurrentActiveWarehouse("MWH.006", invalidReplacePayload()));

    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), ex.getResponse().getStatus());
    assertEquals("Cannot replace warehouse: Warehouse not found", ex.getMessage());
  }

  @Test
  void replaceTheCurrentActiveWarehouse_mapsIllegalArgumentExceptionToBadRequest() {
    var replaceOperation = new RecordingReplaceWarehouseUseCase(new IllegalArgumentException("Warehouse data is invalid"));
    var resource = new WarehouseResourceImpl(
        new NoopArchiveWarehouseUseCase(),
        new NoopCreateWarehouseUseCase(),
        replaceOperation,
        new NoopGetWarehouseOperation(null),
        new RecordingListWarehousesOperation(List.of()),
        mapper);

    var ex = assertThrows(WebApplicationException.class,
        () -> resource.replaceTheCurrentActiveWarehouse("MWH.006", invalidReplacePayload()));

    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
    assertEquals("Cannot replace warehouse: Warehouse data is invalid", ex.getMessage());
  }

  private static Warehouse warehouse(String businessUnitCode, String location, int capacity, int stock) {
    var warehouse = new Warehouse();
    warehouse.businessUnitCode = businessUnitCode;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    return warehouse;
  }

  private static com.warehouse.api.beans.Warehouse apiWarehouse(String businessUnitCode, String location, int capacity, int stock) {
    var warehouse = new com.warehouse.api.beans.Warehouse();
    warehouse.setBusinessUnitCode(businessUnitCode);
    warehouse.setLocation(location);
    warehouse.setCapacity(capacity);
    warehouse.setStock(stock);
    return warehouse;
  }

  private static com.warehouse.api.beans.Warehouse invalidCreatePayload() {
    return apiWarehouse("MWH.003", "Paris", 150, 60);
  }

  private static com.warehouse.api.beans.Warehouse invalidReplacePayload() {
    return apiWarehouse("IGNORED", "Rome", 120, 40);
  }

  private static class NoopArchiveWarehouseUseCase implements ArchiveWarehouseOperation {
    @Override
    public void archive(String businessUnitCode) {
      // no-op
    }
  }

  private static class RecordingArchiveWarehouseUseCase implements ArchiveWarehouseOperation {
    private final RuntimeException toThrow;
    private int calls;
    private String lastArchivedId;

    private RecordingArchiveWarehouseUseCase() {
      this(null);
    }

    private RecordingArchiveWarehouseUseCase(RuntimeException toThrow) {
      this.toThrow = toThrow;
    }

    @Override
    public void archive(String businessUnitCode) {
      calls++;
      lastArchivedId = businessUnitCode;
      if (toThrow != null) {
        throw toThrow;
      }
    }
  }

  private static class NoopCreateWarehouseUseCase implements CreateWarehouseOperation {
    @Override
    public void create(Warehouse warehouse) {
      // no-op
    }
  }

  private static class RecordingCreateWarehouseUseCase implements CreateWarehouseOperation {
    private final RuntimeException toThrow;
    private int calls;
    private Warehouse lastWarehouse;

    private RecordingCreateWarehouseUseCase() {
      this(null);
    }

    private RecordingCreateWarehouseUseCase(RuntimeException toThrow) {
      this.toThrow = toThrow;
    }

    @Override
    public void create(Warehouse warehouse) {
      calls++;
      lastWarehouse = warehouse;
      if (toThrow != null) {
        throw toThrow;
      }
    }
  }

  private static class NoopReplaceWarehouseUseCase implements ReplaceWarehouseOperation {
    @Override
    public void replace(Warehouse warehouse) {
      // no-op
    }
  }

  private static class RecordingReplaceWarehouseUseCase implements ReplaceWarehouseOperation {
    private final RuntimeException toThrow;
    private int calls;
    private Warehouse lastWarehouse;

    private RecordingReplaceWarehouseUseCase() {
      this(null);
    }

    private RecordingReplaceWarehouseUseCase(RuntimeException toThrow) {
      this.toThrow = toThrow;
    }

    @Override
    public void replace(Warehouse warehouse) {
      calls++;
      lastWarehouse = warehouse;
      if (toThrow != null) {
        throw toThrow;
      }
    }
  }

  private static class NoopGetWarehouseOperation implements GetWarehouseOperation {
    private final Warehouse warehouse;
    private String lastRequestedId;

    private NoopGetWarehouseOperation(Warehouse warehouse) {
      this.warehouse = warehouse;
    }

    @Override
    public Warehouse get(String id) {
      lastRequestedId = id;
      return warehouse;
    }
  }

  private static class RecordingGetWarehouseOperation implements GetWarehouseOperation {
    private final Warehouse warehouse;
    private String lastRequestedId;

    private RecordingGetWarehouseOperation(Warehouse warehouse) {
      this.warehouse = warehouse;
    }

    @Override
    public Warehouse get(String id) {
      lastRequestedId = id;
      return warehouse;
    }
  }

  private static class RecordingListWarehousesOperation implements ListWarehousesOperation {
    private final List<Warehouse> warehouses;
    private int calls;

    private RecordingListWarehousesOperation(List<Warehouse> warehouses) {
      this.warehouses = List.copyOf(warehouses);
    }

    @Override
    public List<Warehouse> listWarehouses() {
      calls++;
      return warehouses;
    }
  }
}
