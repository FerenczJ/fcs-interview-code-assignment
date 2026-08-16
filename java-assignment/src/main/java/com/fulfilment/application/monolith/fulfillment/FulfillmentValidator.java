package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@RequestScoped
public class FulfillmentValidator {

  private static final int MAX_WAREHOUSES_PER_PRODUCT_PER_STORE = 2;
  private static final int MAX_WAREHOUSES_PER_STORE = 3;
  private static final int MAX_PRODUCTS_PER_WAREHOUSE = 5;

  private final FulfillmentAssociationRepository associationRepository;
  private final ProductRepository productRepository;

  @Inject
  public FulfillmentValidator(FulfillmentAssociationRepository associationRepository, ProductRepository productRepository) {
    this.associationRepository = associationRepository;
    this.productRepository = productRepository;
  }

  public void validateForLink(FulfillmentAssociation request) {
    validateEntitiesExist(request);
    validateNoDuplicateLink(request);
    validationProductLimitPerStore(request);
    validateStoreLimit(request);
    validatorWarehouseLimit(request);
  }

  private void validateEntitiesExist(FulfillmentAssociation request) {
    if (Store.findById(request.storeId) == null) {
      throw new WebApplicationException("Store does not exist", Response.Status.NOT_FOUND);
    }
    if (productRepository.findById(request.productId) == null) {
      throw new WebApplicationException("Product does not exist", Response.Status.NOT_FOUND);
    }
  }

  private void validateNoDuplicateLink(FulfillmentAssociation request) {
    if (associationRepository.exists(request.storeId, request.productId, request.warehouseBusinessUnitCode)) {
      throw new WebApplicationException("Fulfillment link already exists", Response.Status.CONFLICT);
    }
  }

  private void validationProductLimitPerStore(FulfillmentAssociation request) {
    var warehousesForProductInStore = associationRepository.findByStoreAndProduct(request.storeId, request.productId).stream()
        .map(a -> a.warehouseBusinessUnitCode)
        .distinct()
        .count();
    if (warehousesForProductInStore >= MAX_WAREHOUSES_PER_PRODUCT_PER_STORE) {
      throw new WebApplicationException("Product can only be fulfilled by 2 warehouses per store", Response.Status.CONFLICT);
    }
  }

  private void validateStoreLimit(FulfillmentAssociation request) {
    var warehousesForStore = associationRepository.findByStore(request.storeId).stream()
        .map(a -> a.warehouseBusinessUnitCode)
        .distinct()
        .count();
    if (warehousesForStore >= MAX_WAREHOUSES_PER_STORE) {
      throw new WebApplicationException("Store can only be fulfilled by 3 warehouses", Response.Status.CONFLICT);
    }
  }

  private void validatorWarehouseLimit(FulfillmentAssociation request) {
    var productsForWarehouse = associationRepository.findByWarehouse(request.warehouseBusinessUnitCode).stream()
        .map(a -> a.productId)
        .distinct()
        .count();
    if (productsForWarehouse >= MAX_PRODUCTS_PER_WAREHOUSE) {
      throw new WebApplicationException("Warehouse can only store 5 products", Response.Status.CONFLICT);
    }
  }
}
