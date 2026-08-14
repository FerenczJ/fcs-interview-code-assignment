package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

class StoreResourceTest {

  @Test
  void errorMapper_mapsWebApplicationExceptionToStatusAndPayload() throws Exception {
    var mapper = new StoreResource.ErrorMapper();
    injectObjectMapper(mapper, new ObjectMapper());
    var exception = new WebApplicationException("Store with id of 42 does not exist.", 404);

    try(var response = mapper.toResponse(exception)) {
      var entity = (ObjectNode) response.getEntity();

      assertEquals(404, response.getStatus());
      assertEquals(WebApplicationException.class.getName(), entity.get("exceptionType").asText());
      assertEquals(404, entity.get("code").asInt());
      assertEquals("Store with id of 42 does not exist.", entity.get("error").asText());
    }
  }

  @Test
  void errorMapper_defaultsTo500ForGenericException() throws Exception {
    var mapper = new StoreResource.ErrorMapper();
    injectObjectMapper(mapper, new ObjectMapper());
    var exception = new IllegalStateException("boom");

    try(var response = mapper.toResponse(exception)) {
      var entity = (ObjectNode) response.getEntity();

      assertEquals(500, response.getStatus());
      assertEquals(500, entity.get("code").asInt());
      assertEquals(IllegalStateException.class.getName(), entity.get("exceptionType").asText());
      assertEquals("boom", entity.get("error").asText());
    }
  }

  private static void injectObjectMapper(StoreResource.ErrorMapper mapper, ObjectMapper objectMapper)
      throws Exception {
    var field = StoreResource.ErrorMapper.class.getDeclaredField("objectMapper");
    field.setAccessible(true);
    field.set(mapper, objectMapper);
  }
}
