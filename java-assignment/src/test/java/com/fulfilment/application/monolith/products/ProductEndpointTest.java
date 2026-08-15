package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ProductEndpointTest {

  @Inject ProductResource.ErrorMapper errorMapper;

  @Test
  void testCrudProduct() {
    final String path = "product";

    // List all, should have all 3 products the database has initially:
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));

    // Delete the TONSTAD:
    given().when().delete(path + "/1").then().statusCode(204);

    // List all, TONSTAD should be missing now:
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(not(containsString("TONSTAD")), containsString("KALLAX"), containsString("BESTÅ"));
  }

  @Test
  void getSingle_returns404ForMissingProduct() {
    given()
        .when()
        .get("product/9999")
        .then()
        .statusCode(404)
        .body(containsString("Product with id of 9999 does not exist."));
  }

  @Test
  void create_returns422WhenIdIsSet() {
    given()
        .contentType("application/json")
        .body("{\"id\":1,\"name\":\"Forbidden\"}")
        .when()
        .post("product")
        .then()
        .statusCode(422)
        .body(containsString("Id was invalidly set on request."));
  }

  @Test
  void update_returns422WhenNameMissing() {
    given()
        .contentType("application/json")
        .body("{\"description\":\"Missing name\"}")
        .when()
        .put("product/1")
        .then()
        .statusCode(422)
        .body(containsString("Product Name was not set on request."));
  }

  @Test
  void update_returns404WhenProductDoesNotExist() {
    given()
        .contentType("application/json")
        .body("{\"name\":\"Ghost product\"}")
        .when()
        .put("product/9999")
        .then()
        .statusCode(404)
        .body(containsString("Product with id of 9999 does not exist."));
  }

  @Test
  void delete_returns404WhenProductDoesNotExist() {
    given()
        .when()
        .delete("product/9999")
        .then()
        .statusCode(404)
        .body(containsString("Product with id of 9999 does not exist."));
  }

  @Test
  void errorMapper_mapsWebApplicationExceptionStatusAndMessage() {
    try (var response = errorMapper.toResponse(new WebApplicationException("Boom", 422))) {
      assertEquals(422, response.getStatus());
      var json = response.getEntity().toString();
      assertTrue(json.contains("jakarta.ws.rs.WebApplicationException"));
      assertTrue(json.contains("Boom"));
      assertTrue(json.contains("422"));
    }
  }

  @Test
  void errorMapper_omitsErrorWhenMessageIsNull() {
    try (var response = errorMapper.toResponse(new RuntimeException())) {
      assertEquals(500, response.getStatus());
      var json = response.getEntity().toString();
      assertTrue(json.contains("java.lang.RuntimeException"));
      assertTrue(json.contains("500"));
      assertFalse(json.contains("error"));
    }
  }
}
