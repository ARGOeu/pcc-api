package gr.grnet.pccapi.service;

import gr.grnet.pccapi.client.handle.HandleClient;
import gr.grnet.pccapi.client.handle.HandleClientRequest;
import gr.grnet.pccapi.client.handle.HandleClientResponse;
import gr.grnet.pccapi.dto.handle.HandleRequestDto;
import gr.grnet.pccapi.dto.handle.HandleResponseDto;
import gr.grnet.pccapi.exception.HandleServiceException;
import gr.grnet.pccapi.repository.PrefixRepository;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

@ApplicationScoped
public class HandleService {

    private static final Logger LOG = Logger.getLogger(HandleService.class);

    @Inject
    PrefixRepository prefixRepository;

    public HandleResponseDto create(Integer prefixId, HandleRequestDto request, String username) {

        LOG.infof("Creating Handle under Prefix with ID: %s", prefixId);

        var prefix =
                prefixRepository
                        .findByIdOptional(prefixId)
                        .orElseThrow(() -> new NotFoundException("Prefix not found"));

        var prefixName = prefix.name;
        var suffix = request.getSuffix();

        LOG.infof("Creating Handle: %s/%s", prefixName, suffix);
        LOG.infof(
                "Handle token configured: %s",
                request.getToken() != null && !request.getToken().isBlank());

        var clientRequest = buildClientRequest(prefixName, request, username);

        LOG.info("Calling Handle service...");

        try {
            var handleClient = buildHandleClient(request.getServiceUrl());

            var clientResponse =
                    handleClient.create(
                            "Basic " + request.getToken(),
                            prefixName,
                            suffix,
                            clientRequest);

            LOG.infof(
                    "Handle service response: responseCode=%s, handle=%s",
                    clientResponse.getResponseCode(),
                    clientResponse.getHandle());

            return new HandleResponseDto()
                    .setHandle(clientResponse.getHandle())
                    .setValues(request.getValues());

        } catch (ClientWebApplicationException e) {

            var status = e.getResponse().getStatus();

            HandleClientResponse errorResponse = null;

            try {
                errorResponse = e.getResponse().readEntity(HandleClientResponse.class);
            } catch (Exception ex) {
                LOG.warn("Could not deserialize Handle service error response", ex);
            }

            var message =
                    errorResponse != null && errorResponse.getMessage() != null
                            ? errorResponse.getMessage()
                            : "Handle service request failed.";

            LOG.errorf(
                    "Handle service request failed: status=%s, responseCode=%s, handle=%s, message=%s",
                    status,
                    errorResponse != null ? errorResponse.getResponseCode() : null,
                    errorResponse != null ? errorResponse.getHandle() : null,
                    message);

            throw new HandleServiceException(status, message);
        }
    }

    private HandleClientRequest buildClientRequest(
            String prefix, HandleRequestDto request, String username) {

        List<HandleClientRequest.Value> values = new ArrayList<>();

        var index = 1;

        for (var value : request.getValues()) {
            values.add(
                    new HandleClientRequest.Value(
                            index++,
                            value.getType(),
                            new HandleClientRequest.Data(
                                    "string",
                                    value.getValue())));
        }

        values.add(
                new HandleClientRequest.Value(
                        100,
                        "HS_ADMIN",
                        new HandleClientRequest.Data(
                                "admin",
                                new HandleClientRequest.AdminValue(
                                        prefix + "/" + username,
                                        301,
                                        "011111110011"))));

        return new HandleClientRequest(values);
    }

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



}