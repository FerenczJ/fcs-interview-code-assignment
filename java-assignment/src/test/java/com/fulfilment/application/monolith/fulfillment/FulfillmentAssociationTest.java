package com.fulfilment.application.monolith.fulfillment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.lang.reflect.Constructor;
import org.junit.jupiter.api.Test;

class FulfillmentAssociationTest {

  @Test
  void noArgConstructorExistsForJpa() throws Exception {
    Constructor<FulfillmentAssociation> constructor = FulfillmentAssociation.class.getDeclaredConstructor();

    assertNotNull(constructor);
    assertEquals(0, constructor.getParameterCount());
  }

  @Test
  void constructorPopulatesFields() {
    var association = new FulfillmentAssociation(7L, 11L, "MWH.042");

    assertEquals(7L, association.storeId);
    assertEquals(11L, association.productId);
    assertEquals("MWH.042", association.warehouseBusinessUnitCode);
  }

  @Test
  void entityMappingAnnotationsArePresent() throws Exception {
    assertNotNull(FulfillmentAssociation.class.getAnnotation(Entity.class));
    assertNotNull(FulfillmentAssociation.class.getAnnotation(Table.class));
    assertEquals("fulfillment_association", FulfillmentAssociation.class.getAnnotation(Table.class).name());

    assertNotNull(FulfillmentAssociation.class.getDeclaredField("id").getAnnotation(Id.class));
    assertNotNull(FulfillmentAssociation.class.getDeclaredField("id").getAnnotation(GeneratedValue.class));

    assertEquals(
        false,
        FulfillmentAssociation.class.getDeclaredField("storeId").getAnnotation(Column.class).nullable());
    assertEquals(
        false,
        FulfillmentAssociation.class.getDeclaredField("productId").getAnnotation(Column.class).nullable());
    assertEquals(
        false,
        FulfillmentAssociation.class.getDeclaredField("warehouseBusinessUnitCode")
            .getAnnotation(Column.class)
            .nullable());
  }
}
