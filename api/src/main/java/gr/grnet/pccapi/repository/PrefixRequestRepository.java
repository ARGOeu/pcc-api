package gr.grnet.pccapi.repository;

import gr.grnet.pccapi.entity.Page;
import gr.grnet.pccapi.entity.PageQuery;
import gr.grnet.pccapi.entity.PageQueryImpl;
import gr.grnet.pccapi.entity.Prefix;
import gr.grnet.pccapi.entity.PrefixRequest;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

@ApplicationScoped
public class PrefixRequestRepository implements Repository<PrefixRequest, Integer> {
    //
//    public List<PrefixRequest> findByProviderIds(List<Integer> providerIds) {
//        if (providerIds == null || providerIds.isEmpty()) {
//            return List.of();
//        }
//
//        return find("provider.id IN ?1", providerIds).list();
//    }
    public PageQuery<PrefixRequest> findByProviderIds(
            List<Integer> providerIds,
            String search,
            int page,
            int size) {

        var joiner = new StringJoiner(StringUtils.SPACE);
        joiner.add("from PrefixRequest pr");

        var conditions = new StringJoiner(" AND ");
        var map = new HashMap<String, Object>();

        if (providerIds == null || providerIds.isEmpty()) {
            return new PageQueryImpl<>();
        }

        conditions.add("pr.provider.id IN :providerIds");
        map.put("providerIds", providerIds);

        if (StringUtils.isNotEmpty(search)) {
            conditions.add(
                    "(" +
                            "pr.contactPersonName ilike :search OR " +
                            "pr.contactPersonSurname ilike :search OR " +
                            "pr.contactPersonEmail ilike :search OR " +
                            "pr.organizationName ilike :search OR " +
                            "pr.projectName ilike :search OR " +
                            "pr.projectDescription ilike :search OR " +
                            "pr.provider.name ilike :search" +
                            ")"
            );
            map.put("search", "%" + search + "%");
        }
        joiner.add("WHERE");
        joiner.add(conditions.toString());
        joiner.add("ORDER BY pr.createdAt DESC");

        var panache = find(joiner.toString(), map).page(page, size);

        var pageable = new PageQueryImpl<PrefixRequest>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;
    }

    public List<PrefixRequest> findByContactPersonNameAndSurname(
            String name, String surname) {

        return find("contactPersonName = ?1 AND contactPersonSurname = ?2", name, surname).list();
    }
    public PageQuery<PrefixRequest> findByCreatorPageAndSize(
            String userId,
            String search,
            int page,
            int size) {

        var joiner = new StringJoiner(StringUtils.SPACE);
        joiner.add("from PrefixRequest pr");

        var conditions = new StringJoiner(" AND ");
        var map = new HashMap<String, Object>();

        conditions.add("pr.createdBy = :userId");
        map.put("userId", userId);

        if (StringUtils.isNotEmpty(search)) {
            conditions.add(
                    "(" +
                            "pr.contactPersonName ilike :search OR " +
                            "pr.contactPersonSurname ilike :search OR " +
                            "pr.contactPersonEmail ilike :search OR " +
                            "pr.organizationName ilike :search OR " +
                            "pr.projectName ilike :search OR " +
                            "pr.projectDescription ilike :search OR " +
                            "pr.provider.name ilike :search" +
                            ")"
            );
            map.put("search", "%" + search + "%");
        }

        joiner.add("WHERE");
        joiner.add(conditions.toString());
        joiner.add("ORDER BY pr.createdAt DESC");

        var panache = find(joiner.toString(), map).page(page, size);

        var pageable = new PageQueryImpl<PrefixRequest>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;
    }

    public Optional<PrefixRequest> findByIdAndProviderIds(Integer id, List<Integer> providerIds) {
        if (providerIds == null || providerIds.isEmpty()) {
            return Optional.empty();
        }

        return find("id = ?1 AND provider.id IN ?2", id, providerIds)
                .firstResultOptional();
    }

    public PageQuery<PrefixRequest> findAllPaged(String search, int page, int size) {

        var joiner = new StringJoiner(StringUtils.SPACE);
        joiner.add("from PrefixRequest pr");

        var conditions = new StringJoiner(" AND ");
        var map = new HashMap<String, Object>();

        if (StringUtils.isNotEmpty(search)) {
            conditions.add(
                    "(" +
                            "pr.contactPersonName ilike :search OR " +
                            "pr.contactPersonSurname ilike :search OR " +
                            "pr.contactPersonEmail ilike :search OR " +
                            "pr.contactPersonPhone ilike :search OR " +
                            "pr.organizationName ilike :search OR " +
                            "pr.projectName ilike :search OR " +
                            "pr.projectDescription ilike :search OR " +
                            "pr.provider.name ilike :search" +
                            ")"
            );
            map.put("search", "%" + search + "%");
        }

        if (conditions.length() > 0) {
            joiner.add("WHERE");
            joiner.add(conditions.toString());
        }

        joiner.add("ORDER BY pr.createdAt DESC");

        var panache = find(joiner.toString(), map).page(page, size);

        var pageable = new PageQueryImpl<PrefixRequest>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;
    }
    public List<PrefixRequest> findByCreator(
            String userId) {

        return find("createdBy = ?1", userId).list();
    }

}
