package gr.grnet.pccapi.endpoint;

import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.QUERY;

import gr.grnet.pccapi.dto.CodelistDto;
import gr.grnet.pccapi.service.CodelistService;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@AllArgsConstructor
@Tag(name = "Codelist")
@Path("/codelist")
public class CodelistEndpoint {
  @Inject CodelistService codelistService;

  //  @Tag(name = "Codelist")
  //  @APIResponse(
  //      responseCode = "200",
  //      description = "List of codes objects.",
  //      content =
  //          @Content(schema = @Schema(type = SchemaType.ARRAY, implementation =
  // CodelistDto.class)))
  //  @Operation(summary = "Get a list of all available codes")
  //  @GET
  //  @Produces(MediaType.APPLICATION_JSON)
  //  public Response getAll() {
  //    var codes = codelistService.fetchAll();
  //    return Response.ok().entity(codes).build();
  //  }

  @Tag(name = "Codelist")
  @APIResponse(
      responseCode = "200",
      description = "List of codes objects by category.",
      content =
          @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = CodelistDto.class)))
  @Operation(summary = "Get a list of all available codes")
  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  public Response getByCategory(
      @Parameter(name = "category", in = QUERY, description = "Indicates the category.")
          @QueryParam("category")
          String category) {

    var codes = codelistService.fetchByCategory(category);
    return Response.ok().entity(codes).build();
  }
}
