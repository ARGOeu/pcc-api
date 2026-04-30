package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.dto.APIResponseMsg;
import gr.grnet.pccapi.dto.ProviderResponseDTO;
import gr.grnet.pccapi.service.ProviderService;
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

@Tag(name = "Provider", description = "Provider is an organisation that hosts the handle service")
@Path("/providers")
@Produces(MediaType.APPLICATION_JSON)
public class ProviderEndpoint {

  @Inject ProviderService providerService;

  @GET
  @Operation(summary = "Get all providers")
  @APIResponse(
      responseCode = "200",
      description = "Providers retrieved.",
      content =
          @Content(
              schema =
                  @Schema(type = SchemaType.ARRAY, implementation = ProviderResponseDTO.class)))
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

    return Response.ok(providerService.fetchAll()).build();
  }

  @GET
  @Path("/{id}")
  @Operation(summary = "Get provider by id")
  @APIResponse(
      responseCode = "200",
      description = "Provider retrieved.",
      content =
          @Content(
              schema =
                  @Schema(type = SchemaType.OBJECT, implementation = ProviderResponseDTO.class)))
  @APIResponse(
      responseCode = "401",
      description = "Unauthorized.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
  @APIResponse(
      responseCode = "404",
      description = "Provider not found.",
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

    return Response.ok(providerService.fetchById(id)).build();
  }
}
