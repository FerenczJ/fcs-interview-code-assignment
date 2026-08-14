package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;

public class LocationGateway implements LocationResolver {

  @Override
  public Location resolveByIdentifier(String identifier) {
    return Location.valueOf(identifier);
  }
}
