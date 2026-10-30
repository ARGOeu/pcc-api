package gr.grnet.pccapi.repository;

import gr.grnet.pccapi.dto.statistic.PrefixesPerProviderDto;
import gr.grnet.pccapi.entity.Page;
import gr.grnet.pccapi.entity.PageQuery;
import gr.grnet.pccapi.entity.PageQueryImpl;
import gr.grnet.pccapi.entity.Prefix;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

@ApplicationScoped
public class PrefixRepository implements Repository<Prefix, Integer> {

    /**
     * Checks if the given prefix name has already been used
     *
     * @param name of the prefix
     * @return true or false
     */
    public boolean existsByName(String name) {
        return find("name", name).count() >= 1L;
    }

    /**
     * Return the prefix for the given name
     *
     * @param name of the prefix
     * @return true or false
     */
    public Prefix findByName(String name) {
        return find("name", name).singleResult();
    }

    /**
     * Retrieves a page of Prefixes submitted by the specified user.
     *
     * @param page The index of the page to retrieve (starting from 0).
     * @param size The maximum number of assessments to include in a page.
     * @return A list of Prefixes objects representing the prefixes in the requested page.
     */
    public PageQuery<Prefix> fetchPrefixesByPage(String search, String provider, String domain, String contractType, int page, int size) {

        var joiner = new StringJoiner(StringUtils.SPACE);
        joiner.add("from prefix p");

        var conditions = new StringJoiner(" AND ");
        var map = new HashMap<String, Object>();

        if (StringUtils.isNotEmpty(search)) {
            conditions.add(
                    "(p.name ilike :search "
                            + "or p.owner ilike :search "
                            + "or p.usedBy ilike :search)");
            map.put("search", "%" + search + "%");
        }

        if (StringUtils.isNotEmpty(provider)) {
            conditions.add("p.provider.name ilike :provider");
            map.put("provider", provider);
        }

        if (StringUtils.isNotEmpty(domain)) {
            conditions.add("p.domain.name ilike :domain");
            map.put("domain", domain);
        }

        if (StringUtils.isNotEmpty(contractType)) {
            conditions.add("p.contractType.name ilike :contractType");
            map.put("contractType", contractType);
        }

        if (conditions.length() > 0) {
            joiner.add("WHERE");
            joiner.add(conditions.toString());
        }

        joiner.add("order by p.name ASC");

        var panache = find(joiner.toString(), map).page(page, size);

        var pageable = new PageQueryImpl<Prefix>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;
    }
//
//  public PageQuery<Prefix> fetchPrefixesByProviderIdsAndPage(HashSet<Object> providerIds, String search, String provider, String domain, String contractType, int page, int size) {
//
//    var joiner = new StringJoiner(StringUtils.SPACE);
//    joiner.add("from prefix p");
//
//    var conditions = new StringJoiner(" AND ");
//    var map = new HashMap<String, Object>();
//
//    if (providerIds == null || providerIds.isEmpty()) {
//      conditions.add("1 = 0");
//    } else {
//      conditions.add("str(p.provider.id) in :providerIds");
//      map.put("providerIds", providerIds);
//    }
//
//    if (StringUtils.isNotEmpty(search)) {
//      conditions.add("(p.name ilike :search or p.owner ilike :search or p.usedBy ilike :search)");
//      map.put("search", "%" + search + "%");
//    }
//
//    if (StringUtils.isNotEmpty(provider)) {
//      conditions.add("p.provider.name ilike :provider");
//      map.put("provider", provider);
//    }
//
//    if (StringUtils.isNotEmpty(domain)) {
//      conditions.add("p.domain.name ilike :domain");
//      map.put("domain", domain);
//    }
//
//    if (StringUtils.isNotEmpty(contractType)) {
//      conditions.add("p.contractType.name ilike :contractType");
//      map.put("contractType", contractType);
//    }
//
//    joiner.add("WHERE");
//    joiner.add(conditions.toString());
//    joiner.add("order by p.name ASC");
//
//    var panache = find(joiner.toString(), map).page(page, size);
//
//    var pageable = new PageQueryImpl<Prefix>();
//    pageable.list = panache.list();
//    pageable.index = page;
//    pageable.size = size;
//    pageable.count = panache.count();
//    pageable.page = Page.of(page, size);
//
//    return pageable;
//  }

    public PageQuery<Prefix> fetchPrefixesByAccessibleIdsAndPage(
            Integer providerId,
            Set<Long> providerIds,
            Set<Long> prefixIds,
            String search,
            String provider,
            String domain,
            String contractType,
            int page,
            int size) {

        var conditions = new StringJoiner(" AND ");
        var map = new HashMap<String, Object>();

        var accessConditions = new StringJoiner(" OR ");

        if (providerIds != null && !providerIds.isEmpty()) {
            accessConditions.add("str(p.provider.id) in :providerIds");
            map.put("providerIds", providerIds);
        }

        if (prefixIds != null && !prefixIds.isEmpty()) {
            accessConditions.add("str(p.id) in :prefixIds");
            map.put("prefixIds", prefixIds);
        }

        if (accessConditions.length() == 0) {
            conditions.add("1 = 0");
        } else {
            conditions.add("(" + accessConditions + ")");
        }

        if (providerId != null) {
            conditions.add("p.provider.id = :providerId");
            map.put("providerId", providerId);
        }

        if (StringUtils.isNotEmpty(search)) {
            conditions.add(
                    "(p.name ilike :search or p.owner ilike :search or p.usedBy ilike :search)");
            map.put("search", "%" + search + "%");
        }

        if (StringUtils.isNotEmpty(provider)) {
            conditions.add("p.provider.name ilike :provider");
            map.put("provider", provider);
        }

        if (StringUtils.isNotEmpty(domain)) {
            conditions.add("p.domain.name ilike :domain");
            map.put("domain", domain);
        }

        if (StringUtils.isNotEmpty(contractType)) {
            conditions.add("p.contractType.name ilike :contractType");
            map.put("contractType", contractType);
        }

        var query = "from prefix p WHERE " + conditions + " order by p.name ASC";

        var panache = find(query, map).page(page, size);

        var pageable = new PageQueryImpl<Prefix>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;
    }

    public Optional<Prefix> findByIdAndProviderId(Integer prefixId, Integer providerId) {
        return find("id = ?1 and provider.id = ?2", prefixId, providerId).
                firstResultOptional();
    }

    public boolean existsByProviderId(Integer providerId) {
        return count("provider.id", providerId) > 0;
    }


    public long countPrefixes() {
        return count();
    }

    public List<PrefixesPerProviderDto> countPrefixesPerProvider() {
        return getEntityManager()
                .createQuery(
                        "SELECT new PrefixesPerProviderDto(" +
                                "p.provider.name, COUNT(p)) " +
                                "FROM prefix p " +
                                "WHERE p.provider IS NOT NULL " +
                                "GROUP BY p.provider.id, p.provider.name",
                        PrefixesPerProviderDto.class)
                .getResultList();
    }

    public long countPrefixesByProviderNames(List<String> providerNames) {
        if (providerNames.isEmpty()) {
            return 0;
        }

        return count("provider.name in ?1", providerNames);
    }

    public List<PrefixesPerProviderDto> countPrefixesPerProviderNames(
            List<String> providerNames) {

        if (providerNames == null || providerNames.isEmpty()) {
            return List.of();
        }

        return getSession().createQuery("""
                        SELECT new gr.grnet.pccapi.dto.statistic.PrefixesPerProviderDto(
                            p.provider.name, COUNT(p)
                        )
                        FROM prefix p
                        WHERE p.provider.name IN :providerNames
                        GROUP BY p.provider.name
                        """, PrefixesPerProviderDto.class)
                .setParameter("providerNames", providerNames)
                .getResultList();
    }

    public PageQuery<Prefix> fetchPrefixesByProviderIdAndPage(
            Integer providerId,
            String search,
            String provider,
            String domain,
            String contractType,
            int page,
            int size) {

        var conditions = new StringJoiner(" AND ");
        var map = new HashMap<String, Object>();

        conditions.add("p.provider.id = :providerId");
        map.put("providerId", providerId);

        if (StringUtils.isNotEmpty(search)) {
            conditions.add(
                    "(p.name ilike :search " +
                            "or p.owner ilike :search " +
                            "or p.usedBy ilike :search)"
            );
            map.put("search", "%" + search + "%");
        }

        if (StringUtils.isNotEmpty(provider)) {
            conditions.add("p.provider.name ilike :provider");
            map.put("provider", provider);
        }

        if (StringUtils.isNotEmpty(domain)) {
            conditions.add("p.domain.name ilike :domain");
            map.put("domain", domain);
        }

        if (StringUtils.isNotEmpty(contractType)) {
            conditions.add("p.contractType.name ilike :contractType");
            map.put("contractType", contractType);
        }

        var query = "from prefix p WHERE " + conditions + " order by p.name ASC";

        var panache = find(query, map).page(page, size);

        var pageable = new PageQueryImpl<Prefix>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;
    }

    public PageQuery<Prefix> fetchPrefixesByProviderIdAndPrefixIdsAndPage(
            Integer providerId,
            Set<Integer> prefixIds,
            String search,
            String provider,
            String domain,
            String contractType,
            int page,
            int size) {

        var conditions = new StringJoiner(" AND ");
        var map = new HashMap<String, Object>();

        conditions.add("p.provider.id = :providerId");
        map.put("providerId", providerId);

        if (prefixIds == null || prefixIds.isEmpty()) {
            conditions.add("1 = 0");
        } else {
            conditions.add("p.id in :prefixIds");
            map.put("prefixIds", prefixIds);
        }

        if (StringUtils.isNotEmpty(search)) {
            conditions.add(
                    "(p.name ilike :search " +
                            "or p.owner ilike :search " +
                            "or p.usedBy ilike :search)"
            );
            map.put("search", "%" + search + "%");
        }

        if (StringUtils.isNotEmpty(provider)) {
            conditions.add("p.provider.name ilike :provider");
            map.put("provider", provider);
        }

        if (StringUtils.isNotEmpty(domain)) {
            conditions.add("p.domain.name ilike :domain");
            map.put("domain", domain);
        }

        if (StringUtils.isNotEmpty(contractType)) {
            conditions.add("p.contractType.name ilike :contractType");
            map.put("contractType", contractType);
        }

        var query = "from prefix p WHERE " + conditions +
                " order by p.name ASC";

        var panache = find(query, map).page(page, size);

        var pageable = new PageQueryImpl<Prefix>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;
    }

    public PageQuery<Prefix> fetchPrefixesByProviderIdAndAccessibleResourcesAndPage(
            Integer providerId,
            Set<Integer> providerIds,
            Set<Integer> prefixIds,
            String search,
            String provider,
            String domain,
            String contractType,
            int page,
            int size) {

        var conditions = new StringJoiner(" AND ");
        var map = new HashMap<String, Object>();

        // Always restrict results to the requested provider.
        conditions.add("p.provider.id = :providerId");
        map.put("providerId", providerId);

        // Authorization conditions.
        var accessConditions = new StringJoiner(" OR ");

        // Access to the provider grants access to all its prefixes.
        if (providerIds != null && providerIds.contains(providerId)) {
            accessConditions.add("1 = 1");
        }

        // Access to individual prefixes grants access only to those prefixes.
        if (prefixIds != null && !prefixIds.isEmpty()) {
            accessConditions.add("p.id IN :prefixIds");
            map.put("prefixIds", prefixIds);
        }

        if (accessConditions.length() == 0) {
            conditions.add("1 = 0");
        } else {
            conditions.add("(" + accessConditions + ")");
        }

        // Search filter.
        if (StringUtils.isNotEmpty(search)) {
            conditions.add(
                    "(p.name ilike :search " +
                            "OR p.owner ilike :search " +
                            "OR p.usedBy ilike :search)"
            );
            map.put("search", "%" + search + "%");
        }

        // Provider filter.
        if (StringUtils.isNotEmpty(provider)) {
            conditions.add("p.provider.name ilike :provider");
            map.put("provider", "%" + provider + "%");
        }

        // Domain filter.
        if (StringUtils.isNotEmpty(domain)) {
            conditions.add("p.domain.name ilike :domain");
            map.put("domain", "%" + domain + "%");
        }

        // Contract type filter.
        if (StringUtils.isNotEmpty(contractType)) {
            conditions.add("p.contractType.name ilike :contractType");
            map.put("contractType", "%" + contractType + "%");
        }

        var query = "from prefix p WHERE " + conditions + " order by p.name ASC";

        var panache = find(query, map).page(page, size);

        var pageable = new PageQueryImpl<Prefix>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;
    }
    public Optional<Prefix> findByNameAndProviderId(
            String prefixName,
            Integer providerId) {

        return find(
                "name = ?1 and provider.id = ?2",
                prefixName,
                providerId
        ).firstResultOptional();
    }
    public boolean existsByIdAndProviderId(
            Integer prefixId,
            Integer providerId
    ) {
        return count(
                "id = ?1 and provider.id = ?2",
                prefixId,
                providerId
        ) > 0;
    }
public void updateEpicHealthcheck(Integer prefixId, boolean exists) {
    update("epicHealthcheck = ?1 where id = ?2", exists, prefixId);
}

}
