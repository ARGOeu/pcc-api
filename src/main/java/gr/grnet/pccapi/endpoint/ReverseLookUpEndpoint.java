package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.dto.APIResponseMsg;
import gr.grnet.pccapi.dto.FiltersDto;
import gr.grnet.pccapi.dto.HandleDto;
import gr.grnet.pccapi.enums.Filter;
import gr.grnet.pccapi.enums.LookUpServiceType;
import gr.grnet.pccapi.service.ReverseLookUpService;
import java.util.EnumSet;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Controller that provides all the necessary functionality for looking up handles based on the
 * provided fields
 */
@AllArgsConstructor
@Tag(name = "Reverse LookUp", description = "Provide a proxy endpoint to query the HRLS Service.")
@Path("/reverse-lookup")
public class ReverseLookUpEndpoint {

  ReverseLookUpService reverseLookUpService;

  @Tag(name = "Reverse LookUp")
  @APIResponse(
      responseCode = "200",
      description = "Perform a reverse look up on the HRLS service using the provided filters",
      content =
          @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = HandleDto.class)))
  @APIResponse(
      responseCode = "400",
      description = "The service does not support one of the provided filters.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
  @APIResponse(
      responseCode = "500",
      description = "Internal issue while communicating with HRLS service.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
  @Operation(summary = "Get a list of all available handles")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response search(
      @Parameter(
              description = "The filters to be used in the HRLS lookup.",
              required = true,
              schema = @Schema(type = SchemaType.OBJECT, implementation = FiltersDto.class))
          @RequestBody
          FiltersDto filtersDto) {
    return Response.ok().entity(reverseLookUpService.search(filtersDto)).build();
  }

  @Tag(name = "Reverse LookUp")
  @APIResponse(
      responseCode = "200",
      description = "Return all the supported filters.",
      content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = Filter.class)))
  @Operation(summary = "Get a list of all available filters")
  @GET
  @Path("/filters")
  @Produces(MediaType.APPLICATION_JSON)
  public Response filters() {
    return Response.ok().entity(EnumSet.allOf(Filter.class)).build();
  }

  @Tag(name = "Reverse LookUp")
  @APIResponse(
      responseCode = "200",
      description = "Return all the supported types.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.ARRAY, implementation = LookUpServiceType.class)))
  @Operation(summary = "Get a list of all available lookup service types")
  @GET
  @Path("/types")
  @Produces(MediaType.APPLICATION_JSON)
  public Response types() {
    return Response.ok().entity(EnumSet.allOf(LookUpServiceType.class)).build();
  }
}
