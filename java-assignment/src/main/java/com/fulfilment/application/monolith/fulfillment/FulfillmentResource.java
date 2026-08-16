package com.fulfilment.application.monolith.fulfillment;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("fulfillment")
@RequestScoped
@Produces("application/json")
@Consumes("application/json")
public class FulfillmentResource {

  private final FulfillmentAssociationRepository associationRepository;
  private final FulfillmentValidator validator;

  @Inject
  public FulfillmentResource(FulfillmentAssociationRepository associationRepository, FulfillmentValidator validator) {
    this.associationRepository = associationRepository;
    this.validator = validator;
  }

  @POST
  @Transactional
  public FulfillmentAssociation link(@Valid FulfillmentAssociation  request) {
    validator.validateForLink(request);

    associationRepository.persist(request);
    return request;
  }
}
