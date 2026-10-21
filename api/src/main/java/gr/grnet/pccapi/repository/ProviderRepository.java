package gr.grnet.pccapi.repository;

import gr.grnet.pccapi.entity.Provider;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.List;

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
}
