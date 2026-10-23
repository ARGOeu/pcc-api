package gr.grnet.pccapi.service;

import gr.grnet.pccapi.dto.PrefixRequestInitialDto;
import gr.grnet.pccapi.dto.PrefixRequestResponse;
import gr.grnet.pccapi.dto.PrefixRequestStatusDto;
import gr.grnet.pccapi.entity.PrefixRequest;
import gr.grnet.pccapi.enums.PrefixRequestStatus;
import gr.grnet.pccapi.mapper.PrefixRequestMapper;
import gr.grnet.pccapi.repository.PrefixRequestRepository;
import gr.grnet.pccapi.validator.constraints.CustomValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;
import org.jboss.logging.Logger;
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

    /**
     * Creates a new prefix request with an initial PENDING status.
     *
     * The provider is initially unset because the request has not yet
     * been accepted and associated with a provider.
     *
     * @param request the DTO containing the prefix request details
     * @return the created prefix request as a response DTO
     */
    @Transactional
    public PrefixRequestResponse create(PrefixRequestInitialDto request) {

        LOG.info("Creating new prefix request...");

        var prefixRequest =
                PrefixRequestMapper.INSTANCE.prefixRequestToEntity(request);

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
    public List<PrefixRequestResponse> fetchAll() {

        LOG.info("Fetching all prefix requests...");

        List<PrefixRequest> requests = prefixRequestRepository.listAll();

        // Convert entities into response DTOs.
        return requests.stream()
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
    public PrefixRequestResponse fetchById(Integer id) {

        PrefixRequest prefixRequest = prefixRequestRepository.findById(id);

        if (prefixRequest == null) {
            throw new NotFoundException(
                    "Prefix request not found with id: " + id);
        }

        return PrefixRequestMapper.INSTANCE
                .prefixRequestToResponse(prefixRequest);
    }

    /**
     * Updates the details of an existing prefix request.
     *
     * The mapper updates the editable fields while preserving fields
     * that must not be modified through this operation.
     *
     * @param id the ID of the prefix request to update
     * @param dto the DTO containing the updated request details
     * @return the updated prefix request
     * @throws NotFoundException if no prefix request exists with the given ID
     */
    @Transactional
    public PrefixRequest update(Integer id, PrefixRequestInitialDto dto) {

        PrefixRequest request = prefixRequestRepository.findById(id);

        if (request == null) {
            throw new NotFoundException(
                    "Prefix request with id " + id + " not found");
        }

        // Update the existing entity rather than creating a new one.
        PrefixRequestMapper.INSTANCE.updateEntity(dto, request);

        return request;
    }

    /**
     * Deletes a prefix request by its ID.
     *
     * Accepted requests cannot be deleted.
     *
     * @param id the ID of the prefix request to delete
     * @throws NotFoundException if the prefix request does not exist
     * @throws CustomValidationException if the request has already been accepted
     */
    @Transactional
    public void deleteById(Integer id) {

        PrefixRequest request = prefixRequestRepository.findById(id);

        if (request == null) {
            throw new NotFoundException(
                    "Prefix request not found with id: " + id);
        }

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
     *
     * Only requests with PENDING status can be updated.
     * The new status must be ACCEPTED or REJECTED.
     *
     * @param id the ID of the prefix request
     * @param dto the status dto  to assign
     * @return the updated prefix request response
     * @throws NotFoundException if the request does not exist
     * @throws jakarta.ws.rs.WebApplicationException if the request
     *         is already processed or the new status is invalid
     */
    @Transactional
    public PrefixRequestResponse updateStatus(
            Integer id, PrefixRequestStatusDto dto) {

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

        return PrefixRequestMapper.INSTANCE
                .prefixRequestToResponse(request);
    }
}
