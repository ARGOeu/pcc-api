package gr.grnet.pccapi.service;

import gr.grnet.pccapi.dto.PrefixRequestInitialDto;
import gr.grnet.pccapi.dto.PrefixRequestResponse;
import gr.grnet.pccapi.dto.PrefixRequestStatusDto;
import gr.grnet.pccapi.entity.PrefixRequest;
import gr.grnet.pccapi.enums.PrefixRequestStatus;
import gr.grnet.pccapi.mapper.PrefixRequestMapper;
import gr.grnet.pccapi.repository.PrefixRequestRepository;
import gr.grnet.pccapi.entity.Provider;
import gr.grnet.pccapi.repository.ProviderRepository;
import gr.grnet.pccapi.resources.ProviderResource;
import gr.grnet.pccapi.validator.constraints.CustomValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;
import org.grnet.endpoint.scanner.runtime.context.RoleEndpointHolder;
import org.grnet.endpoint.scanner.runtime.services.ResourceAuthorizationService;
import org.jboss.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
@AllArgsConstructor
public class PrefixRequestService {

    private static final Logger LOG =
            Logger.getLogger(PrefixRequestService.class);

    @Inject
    PrefixRequestRepository prefixRequestRepository;
    @Inject
    ProviderRepository providerRepository;
    @Inject
    AccessControlService accessControlService;

    @Inject
    ResourceAuthorizationService resourceAuthorizationService;

    /**
     * Creates a new prefix request with an initial PENDING status.
     * <p>
     * The provider is initially unset because the request has not yet
     * been accepted and associated with a provider.
     *
     * @param request the DTO containing the prefix request details
     * @return the created prefix request as a response DTO
     */
    @Transactional
    public PrefixRequestResponse create(PrefixRequestInitialDto request, String createdBy) {

        LOG.info("Creating new prefix request...");

        var prefixRequest =
                PrefixRequestMapper.INSTANCE.prefixRequestToEntity(request);

        prefixRequest.createdBy = createdBy;
        // New requests always start in the PENDING state.
        prefixRequest.setStatus(PrefixRequestStatus.PENDING);

        // No status reason or provider is assigned at creation.
        prefixRequest.setStatusReason(null);
        prefixRequest.setProvider(null);

        prefixRequestRepository.persist(prefixRequest);

        return PrefixRequestMapper.INSTANCE
                .prefixRequestToResponse(prefixRequest);
    }

    /**
     * Retrieves all prefix requests.
     *
     * @return a list of prefix request response DTOs
     */
    @Transactional
    public List<PrefixRequestResponse> fetchAll(String userId) {

        LOG.info("Fetching all prefix requests...");

        //List<PrefixRequest> requests = prefixRequestRepository.listAll();
        if (accessControlService.isSuperAdmin()) {
            var prefixRequests = prefixRequestRepository.findAll().list();
            return prefixRequests.stream()
                    .map(PrefixRequestMapper.INSTANCE::prefixRequestToResponse)
                    .collect(Collectors.toList());
        }

        var prefixRequests = prefixRequestRepository.findByCreator(userId);

        return prefixRequests.stream()
                .map(PrefixRequestMapper.INSTANCE::prefixRequestToResponse)
                .collect(Collectors.toList());

        // Convert entities into response DTOs.

    }

    @Transactional
    public List<PrefixRequestResponse> fetchAssigned() {

        LOG.info("Fetching all prefix requests...");

        //List<PrefixRequest> requests = prefixRequestRepository.listAll();
        if (accessControlService.isSuperAdmin()) {
            var prefixRequests = prefixRequestRepository.findAll().list();
            return prefixRequests.stream()
                    .map(PrefixRequestMapper.INSTANCE::prefixRequestToResponse)
                    .collect(Collectors.toList());
        }

        var providerIds = RoleEndpointHolder.get().stream()
                .flatMap(role -> accessControlService
                        .resolveAccessibleGroupsByName(role.getRoleName(), ProviderResource.PROVIDER.resourceName())
                        .stream())
                .map(Integer::valueOf)
                .collect(Collectors.toList());

        var prefixRequests = prefixRequestRepository.findByProviderIds(providerIds);

        return prefixRequests.stream()
                .map(PrefixRequestMapper.INSTANCE::prefixRequestToResponse)
                .collect(Collectors.toList());

    }

    /**
     * Retrieves a prefix request by its ID.
     *
     * @param id the ID of the prefix request
     * @return the matching prefix request as a response DTO
     * @throws NotFoundException if no prefix request exists with the given ID
     */
    @Transactional
    public PrefixRequestResponse fetchById(Integer id, String userId) {

        PrefixRequest prefixRequest = prefixRequestRepository.findById(id);

        if (prefixRequest == null) {
            throw new NotFoundException(
                    "Prefix request not found with id: " + id);
        }

        checkAccess(id, userId);

        return PrefixRequestMapper.INSTANCE
                .prefixRequestToResponse(prefixRequest);
    }

    /**
     * Updates the details of an existing prefix request.
     * <p>
     * The mapper updates the editable fields while preserving fields
     * that must not be modified through this operation.
     *
     * @param id  the ID of the prefix request to update
     * @param dto the DTO containing the updated request details
     * @return the updated prefix request
     * @throws NotFoundException if no prefix request exists with the given ID
     */
    @Transactional
    public PrefixRequestResponse update(
            Integer id,
            PrefixRequestInitialDto dto,
            String userId) {

        PrefixRequest request = prefixRequestRepository.findById(id);

        if (request == null) {
            throw new NotFoundException(
                    "Prefix request with id " + id + " not found");
        }

        checkAccess(id, userId);

        PrefixRequestMapper.INSTANCE.updateEntity(dto, request);

        return PrefixRequestMapper.INSTANCE.toResponse(request);
    }

    /**
     * Deletes a prefix request by its ID.
     * <p>
     * Accepted requests cannot be deleted.
     *
     * @param id the ID of the prefix request to delete
     * @throws NotFoundException         if the prefix request does not exist
     * @throws CustomValidationException if the request has already been accepted
     */
    @Transactional
    public void deleteById(Integer id, String userId) {

        PrefixRequest request = prefixRequestRepository.findById(id);

        if (request == null) {
            throw new NotFoundException(
                    "Prefix request not found with id: " + id);
        }

        checkAccess(id, userId);
        // Accepted requests must be preserved and cannot be deleted.
        if (request.getStatus() == PrefixRequestStatus.ACCEPTED) {
            throw new CustomValidationException(
                    "Request is already accepted and cannot be deleted",
                    409);
        }

        prefixRequestRepository.delete(request);
    }

    /**
     * Updates the status of an existing prefix request.
     * <p>
     * Only requests with PENDING status can be updated.
     * The new status must be ACCEPTED or REJECTED.
     *
     * @param id  the ID of the prefix request
     * @param dto the status dto  to assign
     * @return the updated prefix request response
     * @throws NotFoundException                     if the request does not exist
     * @throws jakarta.ws.rs.WebApplicationException if the request
     *                                               is already processed or the new status is invalid
     */
    @Transactional
    public PrefixRequestResponse updateStatus(
            Integer id, PrefixRequestStatusDto dto, String handledBy) {

        PrefixRequest request = prefixRequestRepository.findById(id);

        if (request == null) {
            throw new NotFoundException(
                    "Prefix request not found with id: " + id);
        }

        // Only pending requests can change status.
        if (request.getStatus() != PrefixRequestStatus.PENDING) {
            throw new CustomValidationException(
                    "Only pending requests can change status", 409);
        }

        // A pending request can only be accepted or rejected.
        if (!Objects.equals(dto.status, PrefixRequestStatus.ACCEPTED.name())
                && !Objects.equals(dto.status, PrefixRequestStatus.REJECTED.name())) {
            throw new jakarta.ws.rs.BadRequestException(
                    "Status can only be changed to ACCEPTED or REJECTED");
        }

        request.setStatus(PrefixRequestStatus.valueOf(dto.status));
        request.setHandledBy(handledBy);
        return PrefixRequestMapper.INSTANCE
                .prefixRequestToResponse(request);
    }

    /**
     * Assigns an existing provider to a prefix request.
     *
     * @param providerId the ID of the provider to assign
     * @return the updated prefix request response
     * @throws NotFoundException if the prefix request or provider does not exist
     */
    @Transactional
    public PrefixRequestResponse assignProvider(Integer requestId, Integer providerId, String handledBy) {

        PrefixRequest request = prefixRequestRepository.findById(requestId);

        if (request == null) {
            throw new NotFoundException(
                    "Prefix request not found with id: " + requestId);
        }

        Provider provider = providerRepository.findById(providerId);

        if (provider == null) {
            throw new NotFoundException(
                    "Provider not found with id: " + providerId);
        }

        request.setProvider(provider);
        request.setHandledBy(handledBy);
        return PrefixRequestMapper.INSTANCE
                .prefixRequestToResponse(request);
    }

    @Transactional
    public void deleteAll() {
        prefixRequestRepository.deleteAll();
    }

    private boolean checkAccess(Integer id, String userId) {

        if (!accessControlService.isSuperAdmin()) {

            var prefixRequests=prefixRequestRepository.findByCreator(userId);

            var exists = prefixRequests.stream()
                    .anyMatch(request -> request.getId().equals(id));

            if (!exists) {
                throw new ForbiddenException("Access denied to prefix request with id: " + id);
            }
        }
        return true;
    }
    private boolean checkProviderAccess(Integer id, String userId) {

        if (!accessControlService.isSuperAdmin()) {

            var providerIds = RoleEndpointHolder.get().stream()
                    .flatMap(role -> accessControlService
                            .resolveAccessibleGroupsByName(role.getRoleName(), ProviderResource.PROVIDER.resourceName())
                            .stream())
                    .map(Integer::valueOf)
                    .collect(Collectors.toList());

            List<PrefixRequest> prefixRequests = new ArrayList<>();
            if (!providerIds.isEmpty()) {
                prefixRequests = prefixRequestRepository.findByProviderIds(providerIds);
            }

            var exists = prefixRequests.stream()
                    .anyMatch(request -> request.getId().equals(id));

            if (!exists) {
                throw new ForbiddenException("Access denied to prefix request with id: " + id);
            }
        }
        return true;
    }
}
