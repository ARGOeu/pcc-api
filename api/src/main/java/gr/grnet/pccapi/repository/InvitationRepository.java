package gr.grnet.pccapi.repository;

import gr.grnet.pccapi.entity.Invitation;
import gr.grnet.pccapi.entity.Page;
import gr.grnet.pccapi.entity.PageQuery;
import gr.grnet.pccapi.entity.PageQueryImpl;
import gr.grnet.pccapi.enums.InvitationStatus;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Repository responsible for managing Invitation entities.
 */
@ApplicationScoped
public class InvitationRepository implements Repository<Invitation, String> {

    /**
     * Retrieves a pending invitation for a specific API resource, resource identifier and email.
     *
     * @param apiResource API resource
     * @param resourceId resource identifier
     * @param email invitation email
     * @return optional pending invitation
     */
    public Optional<Invitation> findPendingInvitation(String apiResource, String resourceId, String email) {

        return find(
                "apiResource = ?1 and resourceId = ?2 and lower(email) = ?3 and status = ?4",
                apiResource,
                resourceId,
                email.toLowerCase(),
                InvitationStatus.PENDING
        ).firstResultOptional();
    }

    /**
     * Retrieves a paginated list of invitations for a specific email.
     *
     * @param email invitation email
     * @param page 0-based page index
     * @param size page size
     * @return paginated invitations
     */
    public PageQuery<Invitation> findAllByEmail(String email, int page, int size) {

        var panache = find(
                "lower(email) = ?1",
                Sort.by("createdAt", Sort.Direction.Descending),
                email.toLowerCase()
        ).page(page, size);

        var pageable = new PageQueryImpl<Invitation>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;
    }

    /**
     * Retrieves a paginated list of invitations with optional API resource,
     * resource identifier, search and sorting.
     *
     * @param apiResource API resource filter
     * @param resourceId resource identifier filter
     * @param search search filter
     * @param sort sort field
     * @param order sort order
     * @param page 0-based page index
     * @param size page size
     * @return paginated invitations
     */
    public PageQuery<Invitation> fetchInvitationsByPageAndSize(String apiResource, String resourceId, String search, String sort, String order, int page, int size) {

        var joiner = new StringJoiner(" ");
        var params = new HashMap<String, Object>();

        joiner.add("SELECT i FROM Invitation i");

        var conditions = new StringJoiner(" AND ");

        if (StringUtils.isNotBlank(apiResource)) {
            conditions.add("i.apiResource = :apiResource");
            params.put("apiResource", apiResource);
        }

        if (StringUtils.isNotBlank(resourceId)) {
            conditions.add("i.resourceId = :resourceId");
            params.put("resourceId", resourceId);
        }

        if (StringUtils.isNotBlank(search)) {
            conditions.add("(i.email ILIKE :search OR i.role ILIKE :search)");
            params.put("search", "%" + search.trim() + "%");
        }

        if (conditions.length() > 0) {
            joiner.add("WHERE " + conditions);
        }

        var allowedSortFields = Set.of(
                "createdAt",
                "email",
                "role",
                "status",
                "apiResource",
                "resourceId"
        );

        if (StringUtils.isBlank(sort) || !allowedSortFields.contains(sort)) {
            sort = "createdAt";
        }

        var direction = "ASC".equalsIgnoreCase(order) ? "ASC" : "DESC";

        joiner.add("ORDER BY i." + sort + " " + direction);

        var panache = find(joiner.toString(), params).page(page, size);

        var result = new PageQueryImpl<Invitation>();
        result.list = panache.list();
        result.index = page;
        result.size = size;
        result.count = panache.count();
        result.page = Page.of(page, size);

        return result;
    }
}