package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.dto.APIResponseMsg;
import gr.grnet.pccapi.dto.DomainDto;
import gr.grnet.pccapi.service.DomainService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = "Domain")
@Path("/domains")
public class DomainEndpoint {

  @Inject DomainService domainService;

  @Operation(summary = "Get domain by id", description = "Returns the domain for the given id.")
  @APIResponse(
      responseCode = "200",
      description = "Domain found",
      content =
          @Content(schema = @Schema(type = SchemaType.OBJECT, implementation = DomainDto.class)))
  @APIResponse(
      responseCode = "401",
      description = "Unauthorized.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
  @APIResponse(
      responseCode = "404",
      description = "Domain not found",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
  @APIResponse(
      responseCode = "500",
      description = "Internal Server Error.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
  @GET
  @Path("/{id}")
  public Response getById(@PathParam("id") Integer id) {

    DomainDto domain = domainService.fetchById(id);

    return Response.ok(domain, MediaType.APPLICATION_JSON).build();
  }

  @Operation(summary = "Get all domains", description = "Returns all available domains.")
  @APIResponse(
      responseCode = "200",
      description = "Domains found",
      content =
          @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = DomainDto.class)))
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
  @GET
  @Path("/")
  public Response getAll() {

    List<DomainDto> domains = domainService.fetchAll();

    return Response.ok(domains, MediaType.APPLICATION_JSON).build();
  }
}
