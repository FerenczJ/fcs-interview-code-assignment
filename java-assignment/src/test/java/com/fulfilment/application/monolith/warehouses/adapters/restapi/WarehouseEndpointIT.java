package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import io.quarkus.test.junit.QuarkusTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class WarehouseEndpointIT {

  @Test
  void testSimpleListWarehouses() {

    final String path = "warehouse";

    // List all, should have all 3 products the database has initially:
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(containsString("MWH.001"), containsString("MWH.012"), containsString("MWH.023"));
  }

  @Test
  void createWarehouse_persistsAndReturnsCreatedWarehouse() {
    final String path = "warehouse";
    final String code = uniqueCode("MWH.IT.CREATE");

    given()
        .contentType("application/json")
        .body(payload(code, "VETSBY-001", 20, 5))
        .when()
        .post(path)
        .then()
        .statusCode(200)
        .body("businessUnitCode", equalTo(code))
        .body("location", equalTo("VETSBY-001"))
        .body("capacity", equalTo(20))
        .body("stock", equalTo(5));

    given()
        .when()
        .get(path + "/" + code)
        .then()
        .statusCode(200)
        .body("businessUnitCode", equalTo(code))
        .body("location", equalTo("VETSBY-001"))
        .body("capacity", equalTo(20))
        .body("stock", equalTo(5));
  }

  @Test
  void createWarehouse_returns400WhenLocationIsInvalid() {
    final String path = "warehouse";
    final String code = uniqueCode("MWH.IT.INVALID");

    given()
        .contentType("application/json")
        .body(payload(code, "NOT-A-REAL-LOCATION", 20, 5))
        .when()
        .post(path)
        .then()
        .statusCode(400)
        .body(containsString("Cannot create warehouse: Location is invalid"));
  }

  @Test
  void getWarehouseById_returns404WhenWarehouseDoesNotExist() {
    final String path = "warehouse";

    given()
        .when()
        .get(path + "/" + uniqueCode("MWH.IT.MISSING"))
        .then()
        .statusCode(404)
        .body(containsString("Warehouse unit not found"));
  }

  @Test
  void deleteWarehouse_archivesAndRemovesItFromList() {
    final String path = "warehouse";
    final String code = uniqueCode("MWH.IT.DELETE");

    given()
        .contentType("application/json")
        .body(payload(code, "HELMOND-001", 15, 2))
        .when()
        .post(path)
        .then()
        .statusCode(200);

    given()
        .when()
        .delete(path + "/" + code)
        .then()
        .statusCode(204);

    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(not(containsString(code)));
  }

  @Test
  void deleteWarehouse_returns404WhenWarehouseDoesNotExist() {
    final String path = "warehouse";

    given()
        .when()
        .delete(path + "/" + uniqueCode("MWH.IT.DELETE.MISSING"))
        .then()
        .statusCode(404)
        .body(containsString("Cannot archive warehouse: Warehouse not found"));
  }

  @Test
  void replaceWarehouse_returnsUpdatedWarehouseAndPreservesPathCode() {
    final String path = "warehouse";
    final String code = uniqueCode("MWH.IT.REPLACE");

    given()
        .contentType("application/json")
        .body(payload(code, "HELMOND-001", 25, 3))
        .when()
        .post(path)
        .then()
        .statusCode(200);

    given()
        .contentType("application/json")
        .body(payload("IGNORED-BODY-CODE", "AMSTERDAM-002", 25, 3))
        .when()
        .post(path + "/" + code + "/replacement")
        .then()
        .statusCode(200)
        .body("businessUnitCode", equalTo("IGNORED-BODY-CODE"))
        .body("location", equalTo("AMSTERDAM-002"))
        .body("capacity", equalTo(25))
        .body("stock", equalTo(3));

    given()
        .when()
        .get(path + "/" + code)
        .then()
        .statusCode(200)
        .body("businessUnitCode", equalTo(code))
        .body("location", equalTo("HELMOND-001"))
        .body("capacity", equalTo(25))
        .body("stock", equalTo(3));
  }

  @Test
  void testSimpleCheckingArchivingWarehouses() {
    final String path = "warehouse";
    final String code = uniqueCode("MWH.IT.ARCHIVE");

    // Use a location that still has room for a new warehouse.
    given()
        .contentType("application/json")
        .body(payload(code, "HELMOND-001", 10, 2))
        .when()
        .post(path)
        .then()
        .statusCode(200);

    given().when().delete(path + "/" + code).then().statusCode(204);

    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(not(containsString(code)));
  }

  private static String payload(String code, String location, int capacity, int stock) {
    return "{" +
        "\"businessUnitCode\":\"" + code + "\"," +
        "\"location\":\"" + location + "\"," +
        "\"capacity\":" + capacity + "," +
        "\"stock\":" + stock +
        "}";
  }

  private static String uniqueCode(String prefix) {
    return prefix + "." + UUID.randomUUID();
  }
}
