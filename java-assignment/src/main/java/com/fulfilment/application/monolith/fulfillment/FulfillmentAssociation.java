package com.fulfilment.application.monolith.fulfillment;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import javax.validation.constraints.NotNull;

@Entity
@Table(name = "fulfillment_association")
@Cacheable
public class FulfillmentAssociation {

  @Id @GeneratedValue public Long id;

  @Column(nullable = false)
  @NotNull
  public Long storeId;

  @Column(nullable = false)
  @NotNull
  public Long productId;

  @Column(nullable = false)
  @NotNull
  public String warehouseBusinessUnitCode;

  public FulfillmentAssociation() {}

  public FulfillmentAssociation(Long storeId, Long productId, String warehouseBusinessUnitCode) {
    this.storeId = storeId;
    this.productId = productId;
    this.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
  }
}
