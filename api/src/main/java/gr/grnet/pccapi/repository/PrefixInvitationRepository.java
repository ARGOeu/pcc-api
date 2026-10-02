package gr.grnet.pccapi.repository;

import gr.grnet.pccapi.entity.Page;
import gr.grnet.pccapi.entity.PageQuery;
import gr.grnet.pccapi.entity.PageQueryImpl;
import gr.grnet.pccapi.entity.PrefixInvitation;
import gr.grnet.pccapi.enums.InvitationStatus;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;


/**
 * Repository responsible for managing PrefixInvitation entities.
 */
@ApplicationScoped
public class PrefixInvitationRepository implements Repository<PrefixInvitation, String> {

    /**
     * Retrieves a pending invitation for a specific provider and email.
     *
     * @param prefixId provider identifier
     * @param email invitation email
     * @return optional pending invitation
     */
    public Optional<PrefixInvitation> findPendingInvitationByPrefixAndEmail(Integer prefixId, String email) {
        return find(
                "prefix.id = ?1 and lower(email) = ?2 and status = ?3",
                prefixId,
                email.toLowerCase(),
                InvitationStatus.PENDING
        ).firstResultOptional();
    }

    /**
     * Retrieves a paginated list of invitation for a specific email.
     *
     * @param email invitation email
     * @param page 0-based page index
     * @param size page size
     * @return paginated invitation
     */
    public PageQuery<PrefixInvitation> findAllByEmail(String email, int page, int size) {

        var panache = find("email = ?1", Sort.by("createdAt", Sort.Direction.Descending), email).page(page, size);

        var pageable = new PageQueryImpl<PrefixInvitation>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;

    }

    /**
     * Retrieves a paginated list of provider invitation with optional search and sorting.
     *
     * @param search search filter
     * @param sort sort field
     * @param order sort order
     * @param page 0-based page index
     * @param size page size
     * @return paginated invitation
     */
    public PageQuery<PrefixInvitation> fetchInvitationsByPageAndSize(String search, String sort, String order, int page, int size) {

        var joiner = new StringJoiner(" ");

        joiner.add("SELECT pi from PrefixInvitation pi")
                .add("left join pi.provider p");

        var params = new HashMap<String, Object>();

        if (StringUtils.isNotBlank(search)) {
            joiner.add("where (p.name ILIKE :search OR pi.email ILIKE :search)");
            params.put("search", "%" + search.trim() + "%");
        }

        var allowedSortFields = Set.of(
                "createdAt",
                "email",
                "name",
                "status"
        );

        if (StringUtils.isBlank(sort) || !allowedSortFields.contains(sort)) {
            sort = "createdAt";
        }

        var direction = "ASC".equalsIgnoreCase(order) ? "ASC" : "DESC";

        var orderByField = "name".equals(sort) ? "p.name" : "pi." + sort;

        joiner.add("order by " + orderByField + " " + direction);

        var panache = find(joiner.toString(), params).page(page, size);

        var result = new PageQueryImpl<PrefixInvitation>();
        result.list = panache.list();
        result.index = page;
        result.size = size;
        result.count = panache.count();
        result.page = Page.of(page, size);

        return result;
    }

    /**
     * Retrieves a paginated list of invitation for a specific provider with optional search and sorting.
     *
     * @param search search filter
     * @param sort sort field
     * @param order sort order
     * @param prefixId provider identifier
     * @param page 0-based page index
     * @param size page size
     * @return paginated provider invitation
     */
    public PageQuery<PrefixInvitation> fetchInvitationsByPrefixByPageAndSize(String search, String sort, String order, Integer prefixId, int page, int size) {

        var joiner = new StringJoiner(" ");
        var params = new HashMap<String, Object>();


        joiner.add("SELECT pi FROM PrefixInvitation pi")
                .add("LEFT JOIN pi.prefix p");

        joiner.add("WHERE p.id = :prefixId");
        params.put("prefixId", prefixId);

        if (StringUtils.isNotBlank(search)) {
            joiner.add("AND (pi.email ILIKE :search or pi.role ILIKE :search)");
            params.put("search", "%" + search.trim() + "%");
        }

        var allowedSortFields = Set.of(
                "createdAt",
                "email",
                "status"
        );

        if (StringUtils.isBlank(sort) || !allowedSortFields.contains(sort)) {
            sort = "createdAt";
        }

        var direction = "ASC".equalsIgnoreCase(order) ? "ASC" : "DESC";

        var orderByField = "pi." + sort;

        joiner.add("order by " + orderByField + " " + direction);

        var panache = find(joiner.toString(), params).page(page, size);

        var result = new PageQueryImpl<PrefixInvitation>();
        result.list = panache.list();
        result.index = page;
        result.size = size;
        result.count = panache.count();
        result.page = Page.of(page, size);

        return result;
    }
}
