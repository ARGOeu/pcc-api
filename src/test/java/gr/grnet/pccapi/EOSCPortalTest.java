package gr.grnet.pccapi;

import gr.grnet.pccapi.client.EOSCPortalClient;
import gr.grnet.pccapi.external.response.EOSCPortalDomain;
import gr.grnet.pccapi.mapper.DomainMapper;
import gr.grnet.pccapi.repository.DomainRepository;
import gr.grnet.pccapi.service.DomainService;
import gr.grnet.pccapi.wiremock.EOSCPortalDomainWiremockServer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(EOSCPortalDomainWiremockServer.class)
@TestProfile(PCCApiTestProfile.class)
public class EOSCPortalTest {

    @Inject
    @RestClient
    EOSCPortalClient eoscPortalClient;

    @Inject
    DomainService domainService;

    @Inject
    DomainRepository domainRepository;

    @Test
    public void retrieveEoscPortalDomains() throws ExecutionException, InterruptedException {

        var eoscPortalDomains = eoscPortalClient.getByType("SCIENTIFIC_DOMAIN").toCompletableFuture().get();

        domainService.saveEoscPortalDomains(eoscPortalDomains);

        assertEquals(8, domainRepository.findAll().list().size());
    }

    @Test
    public void updateEoscPortalDomains() throws ExecutionException, InterruptedException {

        var eoscPortalDomains = eoscPortalClient.getByType("SCIENTIFIC_DOMAIN").toCompletableFuture().get();

        domainService.saveEoscPortalDomains(eoscPortalDomains);

        var eoscPortalDomain = new EOSCPortalDomain();

        eoscPortalDomain.domainId = "scientific_domain-medical_and_health_sciences";
        eoscPortalDomain.name = "updated_name";
        eoscPortalDomain.description = "The science of dealing with the maintenance of health and the prevention and treatment of disease.";

        var existPortalDomain = new EOSCPortalDomain();

        existPortalDomain.domainId = "scientific_domain-social_sciences";
        existPortalDomain.name = "Social Sciences";
        existPortalDomain.description = "A branch of science that deals with the institutions and functioning of human society and with the interpersonal relationships of individuals as members of society.";

        domainService.saveEoscPortalDomains(Set.of(eoscPortalDomain, existPortalDomain));

        assertEquals(DomainMapper.INSTANCE.eoscPortalDomainToDomain(existPortalDomain), domainRepository.findByDomainId("scientific_domain-social_sciences").get());
        assertEquals(DomainMapper.INSTANCE.eoscPortalDomainToDomain(eoscPortalDomain), domainRepository.findByDomainId("scientific_domain-medical_and_health_sciences").get());
    }
}
