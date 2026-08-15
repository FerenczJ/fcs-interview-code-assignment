package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LocationGateway implements LocationResolver {

  @Override
  public Location resolveByIdentifier(String identifier) {
      return Location.fromIdentification(identifier);
  }
}
