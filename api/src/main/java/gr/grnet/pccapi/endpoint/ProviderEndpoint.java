package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.dto.APIResponseMsg;
import gr.grnet.pccapi.dto.InformativeResponse;
import gr.grnet.pccapi.dto.provider.PartialProviderRequest;
import gr.grnet.pccapi.dto.provider.ProviderRequestDto;
import gr.grnet.pccapi.dto.provider.ProviderResponseDTO;
import gr.grnet.pccapi.resources.ProviderResource;
import gr.grnet.pccapi.service.ProviderService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.grnet.endpoint.scanner.runtime.ParamRef;
import org.grnet.endpoint.scanner.runtime.ParamType;
import org.grnet.endpoint.scanner.runtime.Scope;
import org.grnet.endpoint.scanner.runtime.SecuredEndpoint;

@Tag(name = "Provider", description = "Provider is an organisation that hosts the handle service")
@Path("/providers")
@Authenticated
@SecurityScheme(securitySchemeName = "Authentication",
        description = "JWT token",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProviderEndpoint {

  @Inject
  ProviderService providerService;

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
      content = @Content(schema = @Schema(
              type = SchemaType.OBJECT,
              implementation = APIResponseMsg.class)))
  @SecuredEndpoint(
          params = {
                  @ParamRef(
                          param = "id",
                          type = ParamType.PATH,
                          referTo = ProviderResource.class
                  )
          }, scope = {Scope.ALL, Scope.MINE}
  )
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
      description = "Provider not found.",
      content =
          @Content(
              schema = @Schema(type = SchemaType.OBJECT, implementation = APIResponseMsg.class)))
  @APIResponse(
      responseCode = "500",
      description = "Internal Server Error.",
      content =
          @Content(schema = @Schema(
                  type = SchemaType.OBJECT,
                  implementation = APIResponseMsg.class)))
  @SecuredEndpoint(
          params = {
                  @ParamRef(
                          param = "id",
                          type = ParamType.PATH,
                          referTo = ProviderResource.class
                  )
          }
  )
  public Response getById(@PathParam("id") int id) {

    return Response.ok(providerService.fetchById(id)).build();
  }


    @Operation(summary = "Create Provider")
    @APIResponse(
            responseCode = "201",
            description = "Provider created",
            content = @Content(schema = @Schema(
                    implementation = ProviderResponseDTO.class)))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Prefix not found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "409",
            description = "Prefix already exists",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @POST
    @SecuredEndpoint(
            params = {
                    @ParamRef(
                            param = "id",
                            type = ParamType.PATH,
                            referTo = ProviderResource.class
                    )
            }
    )
    public Response create(   @PathParam("id") int id,
                              @Valid ProviderRequestDto providerRequestDto) {

        var response = providerService.create(providerRequestDto);

        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @Operation(summary = "Patch provider")
    @APIResponse(
            responseCode = "200",
            description = "Provider updated",
            content = @Content(schema = @Schema(
                    implementation = ProviderResponseDTO.class)))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Prefix not found",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @PATCH
    @Path("/{provider-id}")
    @SecuredEndpoint(
            params = {
                   @ParamRef(
                            param = "provider-id",
                            type = ParamType.PATH,
                            referTo = ProviderResource.class
                    )
            }
    )
    public Response update(
            @PathParam("id") int id,
            @PathParam("provider-id") int providerId,
            @Valid PartialProviderRequest providerRequestDto) {

        var prefix = providerService.update(providerId, providerRequestDto);

        return Response.ok(prefix).build();
    }
    @Operation(summary = "Delete provider")
    @APIResponse(
            responseCode = "200",
            description = "Provider deleted",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Prefix not found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @DELETE
    @Path("/{provider-id}")
    @SecuredEndpoint(
            params = {
                    @ParamRef(
                            param = "provider-id",
                            type = ParamType.PATH,
                            referTo = ProviderResource.class
                    )
            }
    )
    public Response deleteById(
            @PathParam("provider-id") Integer providerId) {

        providerService.delete(providerId);

        var informativeResponse = new InformativeResponse();
        informativeResponse.code = 200;
        informativeResponse.message = "Provider has been successfully deleted.";

        return Response.ok().entity(informativeResponse).build();
    }

}
