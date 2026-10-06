package gr.grnet.pccapi.repository;

import gr.grnet.pccapi.entity.Provider;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.List;

@ApplicationScoped
public class ProviderRepository implements Repository<Provider, Integer> {

    public List<Provider> fetchProvidersByIds(Collection<Integer> providerIds) {
        return find("id in ?1", providerIds).list();
    }
}
