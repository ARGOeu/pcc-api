package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.dto.APIResponseMsg;
import gr.grnet.pccapi.dto.ProviderResponseDTO;
import gr.grnet.pccapi.service.ProviderService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@AllArgsConstructor
@Tag(name = "Provider", description = "Provider is an organisation that hosts the handle service")
@Path("/v1/providers")
public class ProviderEndpoint {

  ProviderService providerService;

  @Tag(name = "Provider")
  @APIResponse(
      responseCode = "200",
      description = "Get the list of all the available providers in PCC-api.",
      content =
          @Content(
              schema =
                  @Schema(type = SchemaType.ARRAY, implementation = ProviderResponseDTO.class)))
  @Operation(summary = "Get a list of all available providers")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAll() {
    var providers = providerService.fetchAll();
    return Response.ok().entity(providers).build();
  }

  @Tag(name = "Provider")
  @Operation(
      summary = "Get Provider by id",
      description =
          " From here you may get the details of a provider with the requested id in PCC-api .")
  @APIResponse(
      responseCode = "200",
      description = "A successful request" + "retrieving a provider with the given id",
      content =
          @Content(
              schema =
                  @Schema(type = SchemaType.OBJECT, implementation = ProviderResponseDTO.class)))
  @APIResponse(
      responseCode = "404",
      description = "The service cannot find the requested provider.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getById(
      @Parameter(
              description = "The id of the provider to be retrieved.",
              required = true,
              example = "1",
              schema = @Schema(type = SchemaType.INTEGER))
          @PathParam("id")
          int id) {
    var provider = providerService.fetchById(id);
    return Response.ok().entity(provider).build();
  }
}
