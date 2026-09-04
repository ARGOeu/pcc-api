package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.dto.APIResponseMsg;
import gr.grnet.pccapi.dto.CodelistDto;
import gr.grnet.pccapi.service.CodelistService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = "Codelist")
@Path("/codelist")
@Authenticated
@SecurityScheme(securitySchemeName = "Authentication",
        description = "JWT token",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER)
public class CodelistEndpoint {

  @Inject CodelistService codelistService;

  @Operation(
      summary = "Get codelist entries",
      description =
          "Returns all codelist entries, or only entries for the given category when provided.")
  @APIResponse(
      responseCode = "200",
      description = "Codelist entries found",
      content =
          @Content(
              schema = @Schema(type = SchemaType.ARRAY, implementation = CodelistDto.class),
              examples = {
                @ExampleObject(name = "All entries"),
                @ExampleObject(name = "Lookup service types", value = "lookup_service_type"),
                @ExampleObject(name = "Contract types", value = "contract_type")
              }))
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
      description = "Internal Server Error.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
  @GET
  @Path("/")
  public Response getByCategory(@QueryParam("category") String category) {

    List<CodelistDto> codes = codelistService.fetchByCategory(category);

    return Response.ok(codes, MediaType.APPLICATION_JSON).build();
  }
}
