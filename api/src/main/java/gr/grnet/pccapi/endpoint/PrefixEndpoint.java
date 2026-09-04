package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.dto.APIResponseMsg;
import gr.grnet.pccapi.dto.PageResource;
import gr.grnet.pccapi.dto.PartialPrefixDto;
import gr.grnet.pccapi.dto.PrefixDto;
import gr.grnet.pccapi.dto.PrefixResponseDto;
import gr.grnet.pccapi.dto.StatisticsDto;
import gr.grnet.pccapi.dto.StatisticsRequestDto;
import gr.grnet.pccapi.service.PrefixService;
import gr.grnet.pccapi.service.StatisticsService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.text.ParseException;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = "Prefix")
@Path("/prefixes")
@Authenticated
@SecurityScheme(securitySchemeName = "Authentication",
        description = "JWT token",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER)
public class PrefixEndpoint {

  @Inject PrefixService prefixService;

  @Inject StatisticsService statisticsService;

  @POST
  @Operation(summary = "Create prefix")
  @APIResponse(
      responseCode = "201",
      description = "Prefix created",
      content = @Content(schema = @Schema(implementation = PrefixResponseDto.class)))
  @APIResponse(
          responseCode = "401",
          description = "User has not been authenticated.",
          content = @Content(schema = @Schema(
                  type = SchemaType.OBJECT,
                  implementation = APIResponseMsg.class)))
  @APIResponse(
          responseCode = "403",
          description = "Not permitted.",
          content = @Content(schema = @Schema(
                  type = SchemaType.OBJECT,
                  implementation = APIResponseMsg.class)))
  @APIResponse(
      responseCode = "404",
      description = "Resource not found",
      content = @Content(schema = @Schema(implementation = APIResponseMsg.class)))
  @APIResponse(
      responseCode = "409",
      description = "Prefix already exists",
      content = @Content(schema = @Schema(implementation = APIResponseMsg.class)))
  @APIResponse(
      responseCode = "500",
      description = "Internal Server Error",
      content = @Content(schema = @Schema(implementation = APIResponseMsg.class)))
  public Response create(@Valid PrefixDto prefixDto) throws ParseException {

    return Response.status(Response.Status.CREATED).entity(prefixService.create(prefixDto)).build();
  }

  @PUT
  @Path("/{id}")
  @Operation(summary = "Update prefix")
  @APIResponse(
      responseCode = "200",
      description = "Prefix updated",
      content = @Content(schema = @Schema(implementation = PrefixDto.class)))
  @APIResponse(
          responseCode = "401",
          description = "User has not been authenticated.",
          content = @Content(schema = @Schema(
                  type = SchemaType.OBJECT,
                  implementation = APIResponseMsg.class)))
  @APIResponse(
          responseCode = "403",
          description = "Not permitted.",
          content = @Content(schema = @Schema(
                  type = SchemaType.OBJECT,
                  implementation = APIResponseMsg.class)))
  @APIResponse(
      responseCode = "404",
      description = "Prefix not found",
      content = @Content(schema = @Schema(implementation = APIResponseMsg.class)))
  @APIResponse(
      responseCode = "500",
      description = "Internal Server Error",
      content = @Content(schema = @Schema(implementation = APIResponseMsg.class)))
  public Response update(@PathParam("id") int id, @Valid PrefixDto prefixDto) {

    return Response.ok(prefixService.update(prefixDto, id)).build();
  }

  @GET
  @Operation(summary = "Get prefixes")
  @APIResponse(
      responseCode = "200",
      description = "Prefixes found",
      content = @Content(schema = @Schema(implementation = PageableObjects.class)))
  @APIResponse(
          responseCode = "401",
          description = "User has not been authenticated.",
          content = @Content(schema = @Schema(
                  type = SchemaType.OBJECT,
                  implementation = APIResponseMsg.class)))
  @APIResponse(
          responseCode = "403",
          description = "Not permitted.",
          content = @Content(schema = @Schema(
                  type = SchemaType.OBJECT,
                  implementation = APIResponseMsg.class)))
  @APIResponse(
      responseCode = "500",
      description = "Internal Server Error",
      content = @Content(schema = @Schema(implementation = APIResponseMsg.class)))
  public Response getAllByPageAndSize(
      @DefaultValue("1") @Min(1) @QueryParam("page") int page,
      @DefaultValue("10") @Min(1) @Max(100) @QueryParam("size") int size,
      @Context UriInfo uriInfo) {

    return Response.ok(prefixService.fetchByPageAndSize(page - 1, size, uriInfo)).build();
  }

  @PATCH
  @Path("/{id}")
  @Operation(summary = "Patch prefix")
  @APIResponse(
      responseCode = "200",
      description = "Prefix updated",
      content = @Content(schema = @Schema(implementation = PrefixResponseDto.class)))
  @APIResponse(
          responseCode = "401",
          description = "User has not been authenticated.",
          content = @Content(schema = @Schema(
                  type = SchemaType.OBJECT,
                  implementation = APIResponseMsg.class)))
  @APIResponse(
          responseCode = "403",
          description = "Not permitted.",
          content = @Content(schema = @Schema(
                  type = SchemaType.OBJECT,
                  implementation = APIResponseMsg.class)))
  @APIResponse(
      responseCode = "404",
      description = "Prefix not found",
      content = @Content(schema = @Schema(implementation = APIResponseMsg.class)))
  @APIResponse(
      responseCode = "500",
      description = "Internal Server Error",
      content = @Content(schema = @Schema(implementation = APIResponseMsg.class)))
  public Response patch(@PathParam("id") int id, @Valid PartialPrefixDto prefixDto) {

    return Response.ok(prefixService.patchById(id, prefixDto)).build();
  }

  @DELETE
  @Path("/{id}")
  @Operation(summary = "Delete prefix")
  @APIResponse(
      responseCode = "200",
      description = "Prefix deleted",
      content = @Content(schema = @Schema(implementation = APIResponseMsg.class)))
  @APIResponse(
          responseCode = "401",
          description = "User has not been authenticated.",
          content = @Content(schema = @Schema(
                  type = SchemaType.OBJECT,
                  implementation = APIResponseMsg.class)))
  @APIResponse(
          responseCode = "403",
          description = "Not permitted.",
          content = @Content(schema = @Schema(
                  type = SchemaType.OBJECT,
                  implementation = APIResponseMsg.class)))
  @APIResponse(
      responseCode = "404",
      description = "Prefix not found",
      content = @Content(schema = @Schema(implementation = APIResponseMsg.class)))
  @APIResponse(
      responseCode = "500",
      description = "Internal Server Error",
      content = @Content(schema = @Schema(implementation = APIResponseMsg.class)))
  public Response deleteById(@PathParam("id") Integer id) {

    prefixService.deleteById(id);

    return Response.ok(new APIResponseMsg("The Prefix has been successfully deleted.")).build();
  }

  @GET
  @Path("/{id}")
  @Operation(summary = "Get prefix by id")
  @APIResponse(
      responseCode = "200",
      description = "Prefix found",
      content = @Content(schema = @Schema(implementation = PrefixResponseDto.class)))
  @APIResponse(
          responseCode = "401",
          description = "User has not been authenticated.",
          content = @Content(schema = @Schema(
                  type = SchemaType.OBJECT,
                  implementation = APIResponseMsg.class)))
  @APIResponse(
          responseCode = "403",
          description = "Not permitted.",
          content = @Content(schema = @Schema(
                  type = SchemaType.OBJECT,
                  implementation = APIResponseMsg.class)))
  @APIResponse(
      responseCode = "404",
      description = "Prefix not found",
      content = @Content(schema = @Schema(implementation = APIResponseMsg.class)))
  @APIResponse(
      responseCode = "500",
      description = "Internal Server Error",
      content = @Content(schema = @Schema(implementation = APIResponseMsg.class)))
  public Response getById(@PathParam("id") Integer id) {

    return Response.ok(prefixService.fetchById(id)).build();
  }

  @GET
  @Path("/{id}/count")
  @Operation(summary = "Get PID count")
  @APIResponse(responseCode = "200", description = "Count retrieved")
  @APIResponse(
          responseCode = "401",
          description = "User has not been authenticated.",
          content = @Content(schema = @Schema(
                  type = SchemaType.OBJECT,
                  implementation = APIResponseMsg.class)))
  @APIResponse(
          responseCode = "403",
          description = "Not permitted.",
          content = @Content(schema = @Schema(
                  type = SchemaType.OBJECT,
                  implementation = APIResponseMsg.class)))
  @APIResponse(responseCode = "404", description = "Prefix not found")
  @APIResponse(responseCode = "500", description = "Internal Server Error")
  public Response getPIDCountByPrefix(@PathParam("id") String id) {

    return Response.ok(statisticsService.getPIDCountByPrefixID(id)).build();
  }

  @GET
  @Path("/{id}/resolvable")
  @Operation(summary = "Get resolvable PID count")
  @APIResponse(responseCode = "200", description = "Count retrieved")
  @APIResponse(
          responseCode = "401",
          description = "User has not been authenticated.",
          content = @Content(schema = @Schema(
                  type = SchemaType.OBJECT,
                  implementation = APIResponseMsg.class)))
  @APIResponse(
          responseCode = "403",
          description = "Not permitted.",
          content = @Content(schema = @Schema(
                  type = SchemaType.OBJECT,
                  implementation = APIResponseMsg.class)))
  @APIResponse(responseCode = "404", description = "Prefix not found")
  @APIResponse(responseCode = "500", description = "Internal Server Error")
  public Response getResolvablePIDCountByPrefix(@PathParam("id") String id) {

    return Response.ok(statisticsService.getResolvablePIDCountByPrefixID(id)).build();
  }

  @GET
  @Path("/{id}/statistics")
  @Operation(summary = "Get prefix statistics")
  @APIResponse(
      responseCode = "200",
      description = "Statistics retrieved",
      content = @Content(schema = @Schema(implementation = StatisticsDto.class)))
  @APIResponse(
          responseCode = "401",
          description = "User has not been authenticated.",
          content = @Content(schema = @Schema(
                  type = SchemaType.OBJECT,
                  implementation = APIResponseMsg.class)))
  @APIResponse(
          responseCode = "403",
          description = "Not permitted.",
          content = @Content(schema = @Schema(
                  type = SchemaType.OBJECT,
                  implementation = APIResponseMsg.class)))
  @APIResponse(responseCode = "404", description = "Prefix not found")
  @APIResponse(responseCode = "500", description = "Internal Server Error")
  public Response getStatisticsByPrefix(@PathParam("id") String id) {

    return Response.ok(statisticsService.getPrefixStatisticsByID(id)).build();
  }

  @POST
  @Path("/{id}/statistics")
  @Operation(summary = "Set prefix statistics")
  @APIResponse(
      responseCode = "200",
      description = "Statistics saved",
      content = @Content(schema = @Schema(implementation = StatisticsDto.class)))
  @APIResponse(
          responseCode = "401",
          description = "User has not been authenticated.",
          content = @Content(schema = @Schema(
                  type = SchemaType.OBJECT,
                  implementation = APIResponseMsg.class)))
  @APIResponse(
          responseCode = "403",
          description = "Not permitted.",
          content = @Content(schema = @Schema(
                  type = SchemaType.OBJECT,
                  implementation = APIResponseMsg.class)))
  @APIResponse(responseCode = "404", description = "Prefix not found")
  @APIResponse(responseCode = "500", description = "Internal Server Error")
  public Response setStatisticsByPrefix(
      @PathParam("id") String id, StatisticsRequestDto statisticsRequestDto) {

    return Response.ok(statisticsService.setPrefixStatistics(id, statisticsRequestDto)).build();
  }

  public static class PageableObjects extends PageResource<PrefixResponseDto> {

    private List<PrefixResponseDto> content;

    @Override
    public List<PrefixResponseDto> getContent() {
      return content;
    }

    @Override
    public void setContent(List<PrefixResponseDto> content) {
      this.content = content;
    }
  }
}
