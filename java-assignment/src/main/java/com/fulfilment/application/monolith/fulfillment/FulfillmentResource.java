package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@Path("fulfillment")
@RequestScoped
@Produces("application/json")
@Consumes("application/json")
public class FulfillmentResource {

  private static final int MAX_WAREHOUSES_PER_PRODUCT_PER_STORE = 2;
  private static final int MAX_WAREHOUSES_PER_STORE = 3;
  private static final int MAX_PRODUCTS_PER_WAREHOUSE = 5;

  private final FulfillmentAssociationRepository associationRepository;
  private final ProductRepository productRepository;

  @Inject
  public FulfillmentResource(FulfillmentAssociationRepository associationRepository, ProductRepository productRepository) {
    this.associationRepository = associationRepository;
    this.productRepository = productRepository;
  }

  @POST
  @Transactional
  public FulfillmentAssociation link(@Valid FulfillmentAssociation  request) {
    ensureEntitiesExist(request);
    ensureNoDuplicateLink(request);
    ensureProductLimitPerStore(request);
    ensureStoreLimit(request);
    ensureWarehouseLimit(request);

    associationRepository.persist(request);
    return request;
  }

  private void ensureEntitiesExist(FulfillmentAssociation request) {
    if (Store.findById(request.storeId) == null) {
      throw new WebApplicationException("Store does not exist", Response.Status.NOT_FOUND);
    }
    if (productRepository.findById(request.productId) == null) {
      throw new WebApplicationException("Product does not exist", Response.Status.NOT_FOUND);
    }
  }

  private void ensureNoDuplicateLink(FulfillmentAssociation request) {
    if (associationRepository.exists(request.storeId, request.productId, request.warehouseBusinessUnitCode)) {
      throw new WebApplicationException("Fulfillment link already exists", Response.Status.CONFLICT);
    }
  }

  private void ensureProductLimitPerStore(FulfillmentAssociation request) {
    var warehousesForProductInStore = associationRepository.findByStoreAndProduct(request.storeId, request.productId).stream()
        .map(a -> a.warehouseBusinessUnitCode)
        .distinct()
        .count();
    if (warehousesForProductInStore >= MAX_WAREHOUSES_PER_PRODUCT_PER_STORE) {
      throw new WebApplicationException("Product can only be fulfilled by 2 warehouses per store", Response.Status.CONFLICT);
    }
  }

  private void ensureStoreLimit(FulfillmentAssociation request) {
    var warehousesForStore = associationRepository.findByStore(request.storeId).stream()
        .map(a -> a.warehouseBusinessUnitCode)
        .distinct()
        .count();
    if (warehousesForStore >= MAX_WAREHOUSES_PER_STORE) {
      throw new WebApplicationException("Store can only be fulfilled by 3 warehouses", Response.Status.CONFLICT);
    }
  }

  private void ensureWarehouseLimit(FulfillmentAssociation request) {
    var productsForWarehouse = associationRepository.findByWarehouse(request.warehouseBusinessUnitCode).stream()
        .map(a -> a.productId)
        .distinct()
        .count();
    if (productsForWarehouse >= MAX_PRODUCTS_PER_WAREHOUSE) {
      throw new WebApplicationException("Warehouse can only store 5 products", Response.Status.CONFLICT);
    }
  }
}
