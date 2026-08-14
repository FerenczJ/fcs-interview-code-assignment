package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class WarehouseResourceImplTest {

  @Test
  void listAllWarehousesUnits_returnsAllWarehouses() {
    var repository = new StubWarehouseRepository();
    repository.create(warehouse("MWH.001", "ZWOLLE-001", 100, 10));
    repository.create(warehouse("MWH.002", "AMSTERDAM-001", 50, 5));

    var resource = new WarehouseResourceImpl(repository);
    var response = resource.listAllWarehousesUnits();

    assertEquals(2, response.size());
  }

  @Test
  void createANewWarehouseUnit_returnsCreatedWarehouse() {
    var repository = new StubWarehouseRepository();
    var resource = new WarehouseResourceImpl(repository);

    var request = beanWarehouse("MWH.001", "ZWOLLE-001", 40, 10);
    var response = resource.createANewWarehouseUnit(request);

    assertEquals("MWH.001", response.getBusinessUnitCode());
    assertEquals("ZWOLLE-001", response.getLocation());
    assertEquals(40, response.getCapacity());
    assertEquals(10, response.getStock());
  }

  @Test
  void createANewWarehouseUnit_rejectsDuplicateBusinessUnitCodeWithConflict() {
    var repository = new StubWarehouseRepository();
    repository.create(warehouse("MWH.001", "ZWOLLE-001", 100, 10));

    var resource = new WarehouseResourceImpl(repository);

    var ex = assertThrows(WebApplicationException.class,
        () -> resource.createANewWarehouseUnit(beanWarehouse("MWH.001", "AMSTERDAM-001", 50, 5)));
    assertEquals(Response.Status.CONFLICT.getStatusCode(), ex.getResponse().getStatus());
    assertEquals("Business unit code already exists", ex.getMessage());
  }

  @Test
  void createANewWarehouseUnit_rejectsWarehouseCapacityExceedingLocationCapacity() {
    var resource = new WarehouseResourceImpl(new StubWarehouseRepository());

    var ex = assertThrows(WebApplicationException.class,
        () -> resource.createANewWarehouseUnit(beanWarehouse("MWH.001", "ZWOLLE-001", 41, 10)));

    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
    assertEquals("Warehouse capacity exceeds location capacity", ex.getMessage());
  }

  @Test
  void createANewWarehouseUnit_rejectsStockExceedingWarehouseCapacity() {
    var resource = new WarehouseResourceImpl(new StubWarehouseRepository());

    var ex = assertThrows(WebApplicationException.class,
        () -> resource.createANewWarehouseUnit(beanWarehouse("MWH.001", "ZWOLLE-001", 40, 41)));

    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
    assertEquals("Warehouse stock exceeds warehouse capacity", ex.getMessage());
  }

  @Test
  void createANewWarehouseUnit_persistsWarehouseWhenPayloadIsProvided() {
    var repository = new StubWarehouseRepository();
    var resource = new WarehouseResourceImpl(repository);

    var request = beanWarehouse("MWH.001", "ZWOLLE-001", 40, 10);
    resource.createANewWarehouseUnit(request);

    assertEquals(1, repository.getAll().size());
  }

  @Test
  void getAWarehouseUnitByID_returnsMappedWarehouse() {
    var repository = new StubWarehouseRepository();
    repository.create(warehouse("MWH.001", "ZWOLLE-001", 100, 10));

    var resource = new WarehouseResourceImpl(repository);
    var response = resource.getAWarehouseUnitByID("MWH.001");

    assertEquals("MWH.001", response.getBusinessUnitCode());
    assertEquals("ZWOLLE-001", response.getLocation());
  }

  @Test
  void getAWarehouseUnitByID_returnsNotFoundWhenMissing() {
    var resource = new WarehouseResourceImpl(new StubWarehouseRepository());

    var ex = assertThrows(WebApplicationException.class, () -> resource.getAWarehouseUnitByID("MISSING"));
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), ex.getResponse().getStatus());
    assertEquals("Warehouse unit not found", ex.getMessage());
  }

  @Test
  void archiveAWarehouseUnitByID_marksWarehouseAsArchived() {
    var repository = new StubWarehouseRepository();
    repository.create(warehouse("MWH.001", "ZWOLLE-001", 100, 10));
    var resource = new WarehouseResourceImpl(repository);

    resource.archiveAWarehouseUnitByID("MWH.001");

    var archivedWarehouse = repository.findByBusinessUnitCode("MWH.001");
    assertEquals("MWH.001", archivedWarehouse.businessUnitCode);
    assertNotNull(archivedWarehouse.archivedAt);
    assertTrue(
        archivedWarehouse.archivedAt.isAfter(archivedWarehouse.createdAt)
            || archivedWarehouse.archivedAt.isEqual(archivedWarehouse.createdAt));
  }

  @Test
  void archiveAWarehouseUnitByID_returnsNotFoundWhenMissing() {
    var resource = new WarehouseResourceImpl(new StubWarehouseRepository());

    var ex = assertThrows(WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("MISSING"));
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), ex.getResponse().getStatus());
    assertEquals("Warehouse unit not found", ex.getMessage());
  }

  @Test
  void replaceTheCurrentActiveWarehouse_returnsReplacementWarehouse() {
    var repository = new StubWarehouseRepository();
    repository.create(warehouse("MWH.001", "ZWOLLE-001", 100, 10));
    var resource = new WarehouseResourceImpl(repository);

    var response = resource.replaceTheCurrentActiveWarehouse(
        "MWH.001", beanWarehouse("MWH.999", "AMSTERDAM-001", 50, 5));

    assertEquals("MWH.001", response.getBusinessUnitCode());
    assertEquals("AMSTERDAM-001", response.getLocation());
    assertEquals(50, response.getCapacity());
    assertEquals(5, response.getStock());

    var activeWarehouses = repository.listActiveWarehouses();
    assertEquals(1, activeWarehouses.size());
    assertEquals("MWH.001", activeWarehouses.get(0).businessUnitCode);
    assertEquals("AMSTERDAM-001", activeWarehouses.get(0).location);
    assertEquals(50, activeWarehouses.get(0).capacity);
    assertEquals(5, activeWarehouses.get(0).stock);
  }

  @Test
  void replaceTheCurrentActiveWarehouse_returnsNotFoundWhenMissing() {
    var resource = new WarehouseResourceImpl(new StubWarehouseRepository());
    Runnable request = () -> resource.replaceTheCurrentActiveWarehouse(
        "MISSING", beanWarehouse("MWH.999", "AMSTERDAM-001", 50, 5));

    var ex = assertThrows(WebApplicationException.class, request::run);
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), ex.getResponse().getStatus());
    assertEquals("Warehouse unit not found", ex.getMessage());
  }

  @Test
  void replaceTheCurrentActiveWarehouse_returnsBadRequestWhenCapacityExceedsLocationCapacity() {
    var repository = new StubWarehouseRepository();
    repository.create(warehouse("MWH.001", "ZWOLLE-001", 100, 10));
    var resource = new WarehouseResourceImpl(repository);
    Runnable request = () -> resource.replaceTheCurrentActiveWarehouse(
        "MWH.001", beanWarehouse("MWH.999", "ZWOLLE-001", 41, 10));

    var ex = assertThrows(WebApplicationException.class, request::run);
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
    assertEquals("Warehouse capacity exceeds location capacity", ex.getMessage());
  }

  @Test
  void replaceTheCurrentActiveWarehouse_returnsBadRequestWhenStockExceedsWarehouseCapacity() {
    var repository = new StubWarehouseRepository();
    repository.create(warehouse("MWH.001", "ZWOLLE-001", 100, 10));
    var resource = new WarehouseResourceImpl(repository);
    Runnable request = () -> resource.replaceTheCurrentActiveWarehouse(
        "MWH.001", beanWarehouse("MWH.999", "ZWOLLE-001", 40, 41));

    var ex = assertThrows(WebApplicationException.class, request::run);
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
    assertEquals("Warehouse stock exceeds warehouse capacity", ex.getMessage());
  }

  @Test
  void replaceTheCurrentActiveWarehouse_returnsNotFoundWhenPayloadIsInvalidAndTargetWarehouseIsMissing() {
    var resource = new WarehouseResourceImpl(new StubWarehouseRepository());

    var invalidRequest = new com.warehouse.api.beans.Warehouse();
    invalidRequest.setBusinessUnitCode("MWH.999");
    Runnable request = () -> resource.replaceTheCurrentActiveWarehouse("MISSING", invalidRequest);

    var ex = assertThrows(WebApplicationException.class, request::run);
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), ex.getResponse().getStatus());
    assertEquals("Warehouse unit not found", ex.getMessage());
  }

  private static Warehouse warehouse(String code, String location, Integer capacity, Integer stock) {
    var warehouse = new Warehouse();
    warehouse.businessUnitCode = code;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    warehouse.createdAt = LocalDateTime.now();
    return warehouse;
  }

  private static com.warehouse.api.beans.Warehouse beanWarehouse(
      String code, String location, Integer capacity, Integer stock) {
    var warehouse = new com.warehouse.api.beans.Warehouse();
    warehouse.setBusinessUnitCode(code);
    warehouse.setLocation(location);
    warehouse.setCapacity(capacity);
    warehouse.setStock(stock);
    return warehouse;
  }

  private static final class StubWarehouseRepository extends WarehouseRepository {
    private final List<Warehouse> warehouses = new ArrayList<>();

    @Override
    public List<Warehouse> getAll() {
      return List.copyOf(warehouses);
    }

    @Override
    public void create(Warehouse warehouse) {
      warehouses.removeIf(existing -> existing.businessUnitCode.equals(warehouse.businessUnitCode));
      warehouses.add(copy(warehouse));
    }

    @Override
    public void update(Warehouse warehouse) {
      create(warehouse);
    }

    @Override
    public void remove(Warehouse warehouse) {
      warehouses.removeIf(existing -> existing.businessUnitCode.equals(warehouse.businessUnitCode));
    }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
      return warehouses.stream()
          .filter(existing -> existing.businessUnitCode.equals(buCode))
          .findFirst()
          .map(WarehouseResourceImplTest::copy)
          .orElse(null);
    }

    private List<Warehouse> listActiveWarehouses() {
      return warehouses.stream().filter(existing -> existing.archivedAt == null).map(WarehouseResourceImplTest::copy).toList();
    }
  }

  private static Warehouse copy(Warehouse warehouse) {
    var copy = new Warehouse();
    copy.businessUnitCode = warehouse.businessUnitCode;
    copy.location = warehouse.location;
    copy.capacity = warehouse.capacity;
    copy.stock = warehouse.stock;
    copy.createdAt = warehouse.createdAt;
    copy.archivedAt = warehouse.archivedAt;
    return copy;
  }
}
