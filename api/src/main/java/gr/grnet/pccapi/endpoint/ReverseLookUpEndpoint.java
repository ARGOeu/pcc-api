package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.dto.APIResponseMsg;
import gr.grnet.pccapi.dto.FiltersDto;
import gr.grnet.pccapi.dto.HandleDto;
import gr.grnet.pccapi.enums.Filter;
import gr.grnet.pccapi.enums.LookUpServiceType;
import gr.grnet.pccapi.service.ReverseLookUpService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.EnumSet;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = "Reverse LookUp", description = "Provide a proxy endpoint to query the HRLS Service.")
@Path("/reverse-lookup")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReverseLookUpEndpoint {

  @Inject ReverseLookUpService reverseLookUpService;

  @POST
  @Operation(summary = "Search handles")
  @APIResponse(
      responseCode = "200",
      description = "Handles retrieved.",
      content =
          @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = HandleDto.class)))
  @APIResponse(
      responseCode = "400",
      description = "Unsupported filters.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
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
  public Response search(
      @Valid FiltersDto filtersDto,
      @QueryParam("page") @DefaultValue("0") Long page,
      @QueryParam("limit") @DefaultValue("10") Long limit) {

    return Response.ok(reverseLookUpService.search(filtersDto, page, limit)).build();
  }

  @GET
  @Path("/filters")
  @Operation(summary = "Get filters")
  @APIResponse(
      responseCode = "200",
      description = "Filters retrieved.",
      content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = Filter.class)))
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
  public Response filters() {

    return Response.ok(EnumSet.allOf(Filter.class)).build();
  }

  @GET
  @Path("/types")
  @Operation(summary = "Get lookup service types")
  @APIResponse(
      responseCode = "200",
      description = "Types retrieved.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.ARRAY, implementation = LookUpServiceType.class)))
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
  public Response types() {

    return Response.ok(EnumSet.allOf(LookUpServiceType.class)).build();
  }
}
