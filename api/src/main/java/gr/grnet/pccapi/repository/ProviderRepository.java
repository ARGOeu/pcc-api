package gr.grnet.pccapi.repository;

import gr.grnet.pccapi.entity.Provider;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import static java.nio.file.Files.find;

@ApplicationScoped
public class ProviderRepository implements Repository<Provider, Integer> {

    public List<Provider> fetchProvidersByIds(Collection<Integer> providerIds) {
        return find("id in ?1", providerIds).list();
    }

    public boolean existsByName(String name) {
        return count("name", name) > 0;
    }

    public boolean isContactUsedByAnotherProvider(
            Integer contactId, Integer providerId) {

        return find(
                "SELECT DISTINCT p FROM " + Provider.class.getName() +
                        " p JOIN p.contacts c " +
                        "WHERE c.id = ?1 AND p.id <> ?2",
                contactId, providerId)
                .firstResultOptional()
                .isPresent();
    }

    public long countProviders() {
        return count();
    }

    public long countByNames(List<String> providerNames) {
        if (providerNames.isEmpty()) {
            return 0;
        }

        return count("name in ?1", providerNames);
    }

    public long deleteByName(String name) {
        return delete("name", name);
    }


    public List<Provider> fetchProvidersByAccessibleResources(
            Set<Integer> providerIds,
            Set<Integer> prefixIds) {

        if ((providerIds == null || providerIds.isEmpty())
                && (prefixIds == null || prefixIds.isEmpty())) {
            return Collections.emptyList();
        }

        var conditions = new StringJoiner(" OR ");
        var map = new HashMap<String, Object>();

        if (providerIds != null && !providerIds.isEmpty()) {
            conditions.add("p.id IN :providerIds");
            map.put("providerIds", providerIds);
        }

        if (prefixIds != null && !prefixIds.isEmpty()) {
            conditions.add(
                    "EXISTS (" +
                            "SELECT 1 FROM prefix pr " +
                            "WHERE pr.provider.id = p.id " +
                            "AND pr.id IN :prefixIds" +
                            ")"
            );
            map.put("prefixIds", prefixIds);
        }

        return find(
                "FROM provider p WHERE (" + conditions + ") ORDER BY p.name ASC",
                map
        ).list();
    }
}
