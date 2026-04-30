package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.dto.APIResponseMsg;
import gr.grnet.pccapi.dto.ServiceDto;
import gr.grnet.pccapi.service.ServiceService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = "Service")
@Path("/services")
@Produces(MediaType.APPLICATION_JSON)
public class ServiceEndpoint {

  @Inject ServiceService service;

  @GET
  @Operation(summary = "Get all services")
  @APIResponse(
      responseCode = "200",
      description = "Services retrieved.",
      content =
          @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = ServiceDto.class)))
  @APIResponse(
      responseCode = "401",
      description = "Unauthorized.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
  @APIResponse(
      responseCode = "500",
      description = "Internal Server Error.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
  public Response getAll() {

    return Response.ok(service.fetchAll()).build();
  }

  @GET
  @Path("/{id}")
  @Operation(summary = "Get service by id")
  @APIResponse(
      responseCode = "200",
      description = "Service retrieved.",
      content =
          @Content(schema = @Schema(type = SchemaType.OBJECT, implementation = ServiceDto.class)))
  @APIResponse(
      responseCode = "401",
      description = "Unauthorized.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
  @APIResponse(
      responseCode = "404",
      description = "Service not found.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
  @APIResponse(
      responseCode = "500",
      description = "Internal Server Error.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
  public Response getById(@PathParam("id") int id) {

    return Response.ok(service.fetchById(id)).build();
  }
}
