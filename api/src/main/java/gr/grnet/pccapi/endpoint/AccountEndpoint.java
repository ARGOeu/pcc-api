package gr.grnet.pccapi.endpoint;

import gr.grnet.pccapi.dto.InformativeResponse;
import gr.grnet.pccapi.dto.account.AccountRequestDto;
import gr.grnet.pccapi.dto.account.AccountResponseDto;
import gr.grnet.pccapi.dto.invitation.PrefixInvitationResponse;
import gr.grnet.pccapi.dto.pagination.PageResource;
import gr.grnet.pccapi.resources.ProviderResource;
import gr.grnet.pccapi.service.AccountService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.grnet.endpoint.scanner.runtime.ParamRef;
import org.grnet.endpoint.scanner.runtime.ParamType;
import org.grnet.endpoint.scanner.runtime.Scope;
import org.grnet.endpoint.scanner.runtime.SecuredEndpoint;

import java.util.List;

import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.QUERY;

@Path("/providers/{id}/prefixes/{prefix-id}/accounts")
@Tag(name = "Prefix Account")
@Authenticated
@SecurityScheme(
        securitySchemeName = "Authentication",
        description = "JWT token",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountEndpoint {

    @Inject
    AccountService accountService;

    @Operation(summary = "Create account")
    @APIResponse(
            responseCode = "201",
            description = "Account created",
            content = @Content(schema = @Schema(
                    implementation = PageableAccounts.class)))
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
    public Response createAccount(
            @Parameter(
                    description = "Provider identifier.",
                    required = true,
                    example = "1",
                    schema = @Schema(type = SchemaType.INTEGER))
            @PathParam("id") Integer id,

            @Parameter(
                    description = "Prefix identifier.",
                    required = true,
                    example = "1",
                    schema = @Schema(type = SchemaType.INTEGER))
            @PathParam("prefix-id") Integer prefixId,

            @Valid AccountRequestDto request) {

        var response = accountService.createAccount(id, prefixId, request);

        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }

    @Operation(summary = "Get accounts")
    @APIResponse(
            responseCode = "200",
            description = "Accounts found",
            content = @Content(schema = @Schema(
                    type = SchemaType.ARRAY,
                    implementation = AccountResponseDto.class)))
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
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @GET
    @SecuredEndpoint(
            params = {
                    @ParamRef(
                            param = "id",
                            type = ParamType.PATH,
                            referTo = ProviderResource.class
                    )
            },
            scope = {Scope.ALL, Scope.MINE}
    )
    public Response getAccounts(
            @PathParam("id")
            int id,
            @PathParam("prefix-id") Integer prefixId,
            @Parameter(name = "search", in = QUERY,
                    description = "Search accounts by email or endpoint.",
                    example = "user@grnet.gr")
            @QueryParam("search")
            String search,
            @Parameter(name = "page", in = QUERY,
                    description = "Page number. Must be >= 1.")
            @DefaultValue("1")
            @Min(value = 1, message = "Page number must be >= 1.")
            @QueryParam("page")
            int page,
            @Parameter(name = "size", in = QUERY,
                    description = "Page size.")
            @DefaultValue("10")
            @Min(value = 1, message = "Page size must be between 1 and 100.")
            @Max(value = 100, message = "Page size must be between 1 and 100.")
            @QueryParam("size")
            int size,
            @Context UriInfo uriInfo) {

        var accounts = accountService.getAccounts(id, prefixId, search, page - 1, size, uriInfo);

        return Response.ok(accounts).build();
    }

    @Operation(summary = "Get account by id")
    @APIResponse(
            responseCode = "200",
            description = "Account found",
            content = @Content(schema = @Schema(
                    implementation = AccountResponseDto.class)))
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
            description = "Account not found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @GET
    @Path("/{account-id}")
    @SecuredEndpoint(
            params = {
                    @ParamRef(
                            param = "id",
                            type = ParamType.PATH,
                            referTo = ProviderResource.class
                    )
            },
            scope = {Scope.ALL, Scope.MINE}
    )
    public Response getAccountById(
            @Parameter(
                    description = "Provider identifier.",
                    required = true,
                    example = "1",
                    schema = @Schema(type = SchemaType.INTEGER))
            @PathParam("id") Integer id,

            @Parameter(
                    description = "Prefix identifier.",
                    required = true,
                    example = "1",
                    schema = @Schema(type = SchemaType.INTEGER))
            @PathParam("prefix-id") Integer prefixId,

            @Parameter(
                    description = "Account identifier.",
                    required = true,
                    example = "7d9c82ee-6d39-4d47-a42f-1c91e91777c3",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("account-id") String accountId) {

        var account = accountService.getAccountById(id, prefixId, accountId);

        return Response.ok(account).build();
    }

    @Operation(summary = "Update account")
    @APIResponse(
            responseCode = "200",
            description = "Account updated",
            content = @Content(schema = @Schema(
                    implementation = AccountResponseDto.class)))
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
            description = "Account not found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @PUT
    @Path("/{account-id}")
    @SecuredEndpoint(
            params = {
                    @ParamRef(
                            param = "id",
                            type = ParamType.PATH,
                            referTo = ProviderResource.class
                    )
            }
    )
    public Response updateAccount(
            @Parameter(
                    description = "Provider identifier.",
                    required = true,
                    example = "1",
                    schema = @Schema(type = SchemaType.INTEGER))
            @PathParam("id") Integer id,

            @Parameter(
                    description = "Prefix identifier.",
                    required = true,
                    example = "1",
                    schema = @Schema(type = SchemaType.INTEGER))
            @PathParam("prefix-id") Integer prefixId,

            @Parameter(
                    description = "Account identifier.",
                    required = true,
                    example = "7d9c82ee-6d39-4d47-a42f-1c91e91777c3",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("account-id") String accountId,

            @Valid AccountRequestDto request) {

        var updatedAccount = accountService.updateAccount(id, prefixId, accountId, request);

        return Response.ok(updatedAccount).build();
    }

    @Operation(summary = "Delete account")
    @APIResponse(
            responseCode = "200",
            description = "Account deleted",
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
            description = "Account not found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @DELETE
    @Path("/{account-id}")
    @SecuredEndpoint(
            params = {
                    @ParamRef(
                            param = "id",
                            type = ParamType.PATH,
                            referTo = ProviderResource.class
                    )
            }
    )
    public Response deleteAccount(
            @Parameter(
                    description = "Provider identifier.",
                    required = true,
                    example = "1",
                    schema = @Schema(type = SchemaType.INTEGER))
            @PathParam("id") Integer id,

            @Parameter(
                    description = "Prefix identifier.",
                    required = true,
                    example = "1",
                    schema = @Schema(type = SchemaType.INTEGER))
            @PathParam("prefix-id") Integer prefixId,

            @Parameter(
                    description = "Account identifier.",
                    required = true,
                    example = "7d9c82ee-6d39-4d47-a42f-1c91e91777c3",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("account-id") String accountId) {

        accountService.deleteAccount(id, prefixId, accountId);

        var informativeResponse = new InformativeResponse();
        informativeResponse.code = 200;
        informativeResponse.message =
                "Account has been successfully deleted.";

        return Response.ok().entity(informativeResponse).build();
    }


    public static class PageableAccounts extends PageResource<AccountResponseDto> {

        private List<AccountResponseDto> content;

        @Override
        public List<AccountResponseDto> getContent() {
            return content;
        }

        @Override
        public void setContent(List<AccountResponseDto> content) {
            this.content = content;
        }
    }
}