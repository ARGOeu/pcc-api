package gr.grnet.pccapi.service;

import gr.grnet.pccapi.client.handle.HandleClient;
import gr.grnet.pccapi.client.handle.HandleClientRequest;
import gr.grnet.pccapi.client.handle.HandleClientResponse;
import gr.grnet.pccapi.dto.handle.HandleRequestDto;
import gr.grnet.pccapi.dto.handle.HandleResponseDto;
import gr.grnet.pccapi.dto.pagination.PageResource;
import gr.grnet.pccapi.exception.HandleServiceException;
import gr.grnet.pccapi.repository.PrefixRepository;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@ApplicationScoped
public class HandleService {

    private static final Logger LOG = Logger.getLogger(HandleService.class);

    @Inject
    PrefixRepository prefixRepository;

    /**
     * Creates a new Handle under the specified Prefix.
     *
     * @param prefixId the PCC identifier of the Prefix
     * @param serviceUrl the Handle service URL
     * @param handleUsername the Handle administrator username
     * @param token the Handle service authentication token
     * @param request the Handle creation request
     * @return the created Handle
     * @throws NotFoundException if the Prefix does not exist
     * @throws HandleServiceException if the Handle service request fails
     */
    public HandleResponseDto create(Integer prefixId, String serviceUrl, String handleUsername, String token, HandleRequestDto request) {

        LOG.infof("Creating Handle under Prefix with ID: %s", prefixId);

        var prefix = prefixRepository
                .findByIdOptional(prefixId)
                .orElseThrow(() -> new NotFoundException("Prefix not found"));

        var prefixName = prefix.name;
        var suffix = request.getSuffix();

        LOG.infof("Creating Handle: %s/%s", prefixName, suffix);

        var adminHandle = prefixName + "/" + handleUsername;
        var basicUsername = "301%3A" + adminHandle;


        var clientRequest = buildClientRequest(request, adminHandle);

        LOG.info("Calling Handle service...");

        try {
            var handleClient = buildHandleClient(serviceUrl);

            var clientResponse = handleClient.create(buildBasicAuthorization(basicUsername, token), prefixName, suffix, clientRequest);

            LOG.infof(
                    "Handle service response: responseCode=%s, handle=%s",
                    clientResponse.getResponseCode(),
                    clientResponse.getHandle());

            return new HandleResponseDto()
                    .setHandle(clientResponse.getHandle())
                    .setValues(request.getValues());

        } catch (ClientWebApplicationException e) {
            throw handleClientException(e);
        }
    }

    /**
     * Retrieves all Handles registered under the specified Prefix and returns
     * the requested page using PCC pagination.
     *
     * @param prefixId the PCC identifier of the Prefix
     * @param serviceUrl the Handle service URL
     * @param handleUsername the Handle administrator username
     * @param token the Handle service authentication token
     * @param page the requested page number
     * @param size the requested page size
     * @param uriInfo URI information used for generating pagination links
     * @return a paginated list of Handle identifiers
     * @throws NotFoundException if the Prefix does not exist
     * @throws HandleServiceException if the Handle service request fails
     */
    public PageResource<String> fetchAllHandlesByPrefixId(Integer prefixId, String serviceUrl, String handleUsername, String token, String search, int page, int size, UriInfo uriInfo) {

        var prefix = prefixRepository
                .findByIdOptional(prefixId)
                .orElseThrow(() -> new NotFoundException("Prefix not found"));

        try {
            var handleClient = buildHandleClient(serviceUrl);

            var adminHandle = prefix.name + "/" + handleUsername;
            var basicUsername = "301%3A" + adminHandle;

            var clientResponse = handleClient.getAll(buildBasicAuthorization(basicUsername, token), prefix.name);

            var handles = clientResponse.getHandles();

            if (search != null && !search.isBlank()) {
                var searchTerm = search.toLowerCase();

                handles = handles.stream()
                        .filter(handle -> handle.toLowerCase().contains(searchTerm))
                        .toList();
            }

            return new PageResource<>(page, size, handles, uriInfo);

        } catch (ClientWebApplicationException e) {
            throw handleClientException(e);
        }
    }

    // --------------------------------------------------------------------------------------------------------------------------
    // HELPER METHODS
    // --------------------------------------------------------------------------------------------------------------------------

    /**
     * Builds a Handle REST client for the specified Handle service.
     *
     * @param serviceUrl the base URL of the Handle service
     * @return the configured Handle REST client
     */
    private HandleClient buildHandleClient(String serviceUrl) {
        return QuarkusRestClientBuilder.newBuilder()
                .baseUri(URI.create(serviceUrl))
                .build(HandleClient.class);
    }

    /**
     * Builds the request sent to the Handle service.
     *
     * @param request the Handle creation request
     * @param adminHandle the admin Handle administrator
     * @return the request payload expected by the Handle service
     */
    private HandleClientRequest buildClientRequest(
            HandleRequestDto request,
            String adminHandle) {

        List<HandleClientRequest.Value> values = new ArrayList<>();

        var index = 1;

        for (var value : request.getValues()) {
            values.add(new HandleClientRequest.Value(
                    index++,
                    value.getType(),
                    new HandleClientRequest.Data(
                            "string",
                            value.getValue())));
        }

        values.add(new HandleClientRequest.Value(
                100,
                "HS_ADMIN",
                new HandleClientRequest.Data(
                        "admin",
                        new HandleClientRequest.AdminValue(
                                adminHandle,
                                301,
                                "011111110011"))));

        return new HandleClientRequest(values);
    }

    /**
     * Builds the Basic Authorization header used to authenticate against the Handle service.
     *
     * @param basicUsername the Handle authentication username
     * @param token the Handle service authentication token
     * @return the Basic Authorization header
     */
    private String buildBasicAuthorization(String basicUsername, String token) {
        var credentials = basicUsername + ":" + token;

        return "Basic " + Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Converts a Handle service client error into a {@link HandleServiceException}.
     *
     * @param e the exception returned by the Handle service client
     * @return a Handle service exception containing the downstream status and message
     */
    private HandleServiceException handleClientException(ClientWebApplicationException e) {

        var status = e.getResponse().getStatus();

        HandleClientResponse errorResponse = null;

        try {
            errorResponse = e.getResponse().readEntity(HandleClientResponse.class);
        } catch (Exception ex) {
            LOG.warn("Could not deserialize Handle service error response", ex);
        }

        var message = errorResponse != null && errorResponse.getMessage() != null
                ? errorResponse.getMessage()
                : "Handle service request failed.";

        LOG.errorf(
                "Handle service request failed: status=%s, responseCode=%s, handle=%s, message=%s",
                status,
                errorResponse != null ? errorResponse.getResponseCode() : null,
                errorResponse != null ? errorResponse.getHandle() : null,
                message);

        return new HandleServiceException(status, message);
    }
}