package com.fulfilment.application.monolith.fulfillment;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import com.fulfilment.application.monolith.products.Product;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class FulfillmentResourceIT {

  private static final String PATH = "fulfillment";

  @Inject EntityManager entityManager;

  @BeforeEach
  @Transactional
  void cleanAssociations() {
    entityManager.createQuery("delete from FulfillmentAssociation").executeUpdate();
    entityManager
        .createNativeQuery("ALTER SEQUENCE fulfillment_association_seq RESTART WITH 1")
        .executeUpdate();
  }

  @Test
  void link_returnsCreatedAssociationWhenRequestIsValid() {
    given()
        .contentType(ContentType.JSON)
        .body(payload(1L, 1L, "MWH.001"))
        .when()
        .post(PATH)
        .then()
        .statusCode(200)
        .body("id", notNullValue())
        .body("storeId", is(1))
        .body("productId", is(1))
        .body("warehouseBusinessUnitCode", is("MWH.001"));
  }

  @Test
  void link_returns404WhenStoreDoesNotExist() {
    given()
        .contentType(ContentType.JSON)
        .body(payload(9999L, 1L, "MWH.001"))
        .when()
        .post(PATH)
        .then()
        .statusCode(404)
        .body(containsString("Store does not exist"));
  }

  @Test
  void link_returns404WhenProductDoesNotExist() {
    given()
        .contentType(ContentType.JSON)
        .body(payload(1L, 9999L, "MWH.001"))
        .when()
        .post(PATH)
        .then()
        .statusCode(404)
        .body(containsString("Product does not exist"));
  }

  @Test
  void link_returns409WhenAssociationAlreadyExists() {
    seedAssociation(1L, 1L, "MWH.001");

    given()
        .contentType(ContentType.JSON)
        .body(payload(1L, 1L, "MWH.001"))
        .when()
        .post(PATH)
        .then()
        .statusCode(409)
        .body(containsString("Fulfillment link already exists"));
  }

  @Test
  void link_returns409WhenProductAlreadyFulfilledByTwoWarehousesForStore() {
    seedAssociation(1L, 1L, "MWH.001");
    seedAssociation(1L, 1L, "MWH.012");

    given()
        .contentType(ContentType.JSON)
        .body(payload(1L, 1L, "MWH.023"))
        .when()
        .post(PATH)
        .then()
        .statusCode(409)
        .body(containsString("Product can only be fulfilled by 2 warehouses per store"));
  }

  @Test
  void link_returns409WhenStoreAlreadyFulfilledByThreeWarehouses() {
    seedAssociation(1L, 1L, "MWH.001");
    seedAssociation(1L, 2L, "MWH.012");
    seedAssociation(1L, 3L, "MWH.023");

    given()
        .contentType(ContentType.JSON)
        .body(payload(1L, 1L, "MWH.999"))
        .when()
        .post(PATH)
        .then()
        .statusCode(409)
        .body(containsString("Store can only be fulfilled by 3 warehouses"));
  }

  @Test
  void link_returns409WhenWarehouseAlreadyStoresFiveProducts() {
    Long p1 = ensureExtraProduct("FULFILL-P1");
    Long p2 = ensureExtraProduct("FULFILL-P2");
    Long p3 = ensureExtraProduct("FULFILL-P3");
    Long p4 = ensureExtraProduct("FULFILL-P4");
    Long p5 = ensureExtraProduct("FULFILL-P5");

    seedAssociation(1L, p1, "MWH.001");
    seedAssociation(1L, p2, "MWH.001");
    seedAssociation(1L, p3, "MWH.001");
    seedAssociation(2L, p4, "MWH.001");
    seedAssociation(2L, p5, "MWH.001");

    given()
        .contentType(ContentType.JSON)
        .body(payload(3L, 1L, "MWH.001"))
        .when()
        .post(PATH)
        .then()
        .statusCode(409)
        .body(containsString("Warehouse can only store 5 products"));
  }

  @Test
  void link_allowsSameWarehouseForDifferentProductsOnSameStore() {
    seedAssociation(1L, 1L, "MWH.001");

    given()
        .contentType(ContentType.JSON)
        .body(payload(1L, 2L, "MWH.001"))
        .when()
        .post(PATH)
        .then()
        .statusCode(200)
        .body("storeId", is(1))
        .body("productId", is(2))
        .body("warehouseBusinessUnitCode", is("MWH.001"));
  }

  @Transactional
  void seedAssociation(Long storeId, Long productId, String warehouseBusinessUnitCode) {
    entityManager.persist(new FulfillmentAssociation(storeId, productId, warehouseBusinessUnitCode));
    entityManager.flush();
  }

  @Transactional
  Long ensureExtraProduct(String name) {
    Product existing =
        entityManager
            .createQuery("from Product p where p.name = :name", Product.class)
            .setParameter("name", name)
            .getResultStream()
            .findFirst()
            .orElse(null);
    if (existing != null) {
      return existing.id;
    }

    Product product = new Product(name);
    entityManager.persist(product);
    entityManager.flush();
    return product.id;
  }

  private static String payload(Long storeId, Long productId, String warehouseBusinessUnitCode) {
    return """
        {
          "storeId": %d,
          "productId": %d,
          "warehouseBusinessUnitCode": "%s"
        }
        """
        .formatted(storeId, productId, warehouseBusinessUnitCode);
  }
}
