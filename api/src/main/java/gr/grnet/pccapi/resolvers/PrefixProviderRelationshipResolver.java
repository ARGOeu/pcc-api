package gr.grnet.pccapi.resolvers;

import gr.grnet.pccapi.enums.resources.PrefixResource;
import gr.grnet.pccapi.enums.resources.ProviderResource;
import gr.grnet.pccapi.repository.PrefixRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.grnet.endpoint.scanner.runtime.ResourceRelationshipResolver;

@ApplicationScoped
public class PrefixProviderRelationshipResolver  implements ResourceRelationshipResolver {

    @Inject
    PrefixRepository prefixRepository;

    @Override
    public String getSourceResourceName() {
        return PrefixResource.PREFIX.resourceName();
    }

    @Override
    public String getTargetResourceName() {
        return ProviderResource.PROVIDER.resourceName();
    }

    @Override
    public boolean isRelatedTo(
            String sourceResourceId,
            String targetResourceId) {

        return prefixRepository.existsByIdAndProviderId(
                Integer.valueOf(sourceResourceId),
                Integer.valueOf(targetResourceId)
        );
    }
}