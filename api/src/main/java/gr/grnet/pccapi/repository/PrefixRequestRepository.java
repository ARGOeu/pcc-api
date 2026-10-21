package gr.grnet.pccapi.repository;

import gr.grnet.pccapi.entity.PrefixRequest;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class PrefixRequestRepository implements Repository<PrefixRequest, Integer> {
    public List<PrefixRequest> findByProviderIds(List<Integer> providerIds) {
        if (providerIds == null || providerIds.isEmpty()) {
            return List.of();
        }

        return find("provider.id IN ?1", providerIds).list();
    }

    public List<PrefixRequest> findByContactPersonNameAndSurname(
            String name, String surname) {

        return find("contactPersonName = ?1 AND contactPersonSurname = ?2", name, surname).list();
    }

    public List<PrefixRequest> findByCreator(
            String userId) {

        return find("createdBy = ?1", userId).list();
    }
}
