package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.dto.APIResponseMsg;
import gr.grnet.pccapi.dto.PartialPrefixDto;
import gr.grnet.pccapi.dto.PrefixDto;
import gr.grnet.pccapi.dto.PrefixResponseDto;
import gr.grnet.pccapi.dto.ProviderResponseDTO;
import gr.grnet.pccapi.dto.StatisticsDto;
import gr.grnet.pccapi.dto.StatisticsRequestDto;
import gr.grnet.pccapi.service.PrefixService;
import gr.grnet.pccapi.service.StatisticsService;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.text.ParseException;
import lombok.AllArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/** Controller that provides all the prefix related functionality */
@AllArgsConstructor
@Tag(name = "Prefix")
@Path("/v1/prefixes")
public class PrefixEndpoint {

  PrefixService prefixService;
  StatisticsService statisticsService;

  @Operation(summary = "Create a new Prefix.")
  @APIResponse(
      responseCode = "201",
      description = "The particular Prefix.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = PrefixResponseDto.class)))
  @APIResponse(
      responseCode = "404",
      description = "The service cannot find the requested link resources.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
  @APIResponse(
      responseCode = "409",
      description = "The service already has a prefix with same values.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response create(
      @Parameter(
              description = "The Prefix to be saved to the service.",
              required = true,
              schema = @Schema(type = SchemaType.OBJECT, implementation = PrefixDto.class))
          @Valid
          @RequestBody
          PrefixDto prefixDto)
      throws ParseException {
    return Response.status(Response.Status.CREATED).entity(prefixService.create(prefixDto)).build();
  }

  @Tag(name = "Prefix")
  @Operation(
      summary = "Update prefix",
      description = " From here you may update the prefix fields .")
  @APIResponse(
      responseCode = "200",
      description = "A successful request" + "update a prefix with a given  id",
      content =
          @Content(
              schema =
                  @Schema(type = SchemaType.OBJECT, implementation = ProviderResponseDTO.class)))
  @APIResponse(
      responseCode = "404",
      description = "The service cannot find the requested link resources.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
  @PUT
  @Path("/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public PrefixDto update(
      @Parameter(
              description = "The id of the prefix to be updated.",
              required = true,
              example = "1",
              schema = @Schema(type = SchemaType.INTEGER))
          @PathParam("id")
          int id,
      @Valid @RequestBody PrefixDto prefixDto) {
    // fully update a prefix of a specific id
    return prefixService.update(prefixDto, id);
  }

  @Tag(name = "Prefix")
  @APIResponse(
      responseCode = "200",
      description = "Get the list of all the available prefixes in PCC-api.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.ARRAY, implementation = PrefixResponseDto.class)))
  @Operation(summary = "Get a list of all available prefixes")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAll() {
    var prefixes = prefixService.fetchAll();
    return Response.ok().entity(prefixes).build();
  }

  @Operation(summary = "Partially update a particular Prefix.")
  @APIResponse(
      responseCode = "200",
      description = "The Prefix has been successfully updated.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = PrefixResponseDto.class)))
  @APIResponse(
      responseCode = "404",
      description = "Some entity not found during partial update.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
  @PATCH
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response patch(
      @Parameter(
              description = "The Predix ID to be partially updated.",
              required = true,
              example = "1",
              schema = @Schema(type = SchemaType.INTEGER))
          @PathParam("id")
          int id,
      @Valid @RequestBody PartialPrefixDto prefixDto) {
    return Response.status(Response.Status.OK)
        .entity(prefixService.patchById(id, prefixDto))
        .build();
  }

  @Operation(
      summary = "Delete a particular Prefix.",
      description =
          "Passing the unique Prefix ID generated by the database, you can delete the corresponding Prefix.")
  @APIResponse(
      responseCode = "200",
      description = "The Prefix has been successfully deleted.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
  @APIResponse(
      responseCode = "404",
      description = "Prefix not Found.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
  @DELETE
  @Path("/{id}")
  public Response deleteById(
      @Parameter(
              description = "The Predix ID to be deleted.",
              required = true,
              example = "1",
              schema = @Schema(type = SchemaType.INTEGER))
          @PathParam("id")
          Integer id) {

    var responseMsg = new APIResponseMsg("The Prefix has been successfully deleted.");
    prefixService.deleteById(id);
    return Response.ok().entity(responseMsg).build();
  }

  @Operation(
      summary = "Fetch a particular Prefix.",
      description =
          "Passing the unique Prefix ID generated by the database, you can fetch the corresponding Prefix.")
  @APIResponse(
      responseCode = "200",
      description = "The particular Prefix.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = PrefixResponseDto.class)))
  @APIResponse(
      responseCode = "404",
      description = "Prefix not Found.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getById(
      @Parameter(
              description = "The Prefix ID to be retrieved.",
              required = true,
              example = "1",
              schema = @Schema(type = SchemaType.INTEGER))
          @PathParam("id")
          Integer id) {

    var prefix = prefixService.fetchById(id);

    return Response.ok().entity(prefix).build();
  }

  @Operation(
      summary = "Fetch the count of PIDs for a particular Prefix.",
      description =
          "Passing the unique Prefix ID, you can fetch the count of PIDs for the particular Prefix.")
  @APIResponse(
      responseCode = "200",
      description = "The count of PIDs for the particular Prefix.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = PrefixResponseDto.class)))
  @APIResponse(
      responseCode = "404",
      description = "Prefix not Found.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
  @GET
  @Path("/{id}/count")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPIDCountByPrefix(
      @Parameter(
              description = "The Prefix ID for which the PIDs count will be retrieved.",
              required = true,
              example = "1",
              schema = @Schema(type = SchemaType.STRING))
          @PathParam("id")
          String id) {
    var count = statisticsService.getPIDCountByPrefixID(id);
    return Response.ok().entity(count).build();
  }

  @Operation(
      summary = "Fetch the count of resolvable PIDs for a particular Prefix.",
      description =
          "Passing the unique Prefix ID, you can fetch the count of resolvable PIDs for the particular Prefix.")
  @APIResponse(
      responseCode = "200",
      description = "The count of resolvable PIDs for the particular Prefix.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = PrefixResponseDto.class)))
  @APIResponse(
      responseCode = "404",
      description = "Prefix not Found.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
  @GET
  @Path("/{id}/resolvable")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getResolvablePIDCountByPrefix(
      @Parameter(
              description = "The Prefix ID for which the resolvable PIDs count will be retrieved.",
              required = true,
              example = "1",
              schema = @Schema(type = SchemaType.STRING))
          @PathParam("id")
          String id) {
    var count = statisticsService.getResolvablePIDCountByPrefixID(id);
    return Response.ok().entity(count).build();
  }

  @GET
  @Path("/{id}/statistics")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getStatisticsByPrefix(
      @Parameter(
              description = "The Prefix ID for which the statistics will be retrieved.",
              required = true,
              example = "1",
              schema = @Schema(type = SchemaType.STRING))
          @PathParam("id")
          String id) {
    var count = statisticsService.getPrefixStatisticsByID(id);
    return Response.ok().entity(count).build();
  }

  @Operation(
      summary = "Set Statistics  for a particular Prefix.",
      description =
          "Passing the unique Prefix ID, you can set statistics for the particular Prefix.")
  @APIResponse(
      responseCode = "200",
      description = "The statistics for the particular Prefix.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = StatisticsDto.class)))
  @APIResponse(
      responseCode = "404",
      description = "Prefix not Found.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
  @POST
  @Path("/{id}/statistics")
  @Produces(MediaType.APPLICATION_JSON)
  public Response setStatisticsByPrefix(
      @Parameter(
              description = "The Prefix Name for which the statistics will be retrieved.",
              required = true,
              example = "123.122",
              schema = @Schema(type = SchemaType.STRING))
          @PathParam("id")
          String id,
      @Parameter(
              description = "The Statistics of the prefix",
              required = true,
              schema = @Schema(type = SchemaType.OBJECT))
          @RequestBody
          StatisticsRequestDto statisticsRequestDto) {
    StatisticsDto resp = null;
    resp = statisticsService.setPrefixStatistics(id, statisticsRequestDto);

    return Response.ok().entity(resp).build();
  }
}
