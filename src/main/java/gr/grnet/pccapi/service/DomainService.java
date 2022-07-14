package gr.grnet.pccapi.service;

import gr.grnet.pccapi.external.response.EOSCPortalDomain;
import gr.grnet.pccapi.mapper.DomainMapper;
import gr.grnet.pccapi.repository.DomainRepository;
import gr.grnet.pccapi.entity.Domain;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Set;

import org.jboss.logging.Logger;

@ApplicationScoped
public class DomainService {

    @Inject
    DomainRepository domainRepository;

    @Inject
    Logger LOG;

    @Transactional
    /**
     * It accepts the EOSC-Portal domains, turns them into {@link Domain domains} and then stores the transformed Domains into the database.
     * If the EOSC-Portal domain exists in the database, then updates its attributes.
     * @param eoscPortalDomains The {@link EOSCPortalDomain eoscPortalDomains} collected by EOSC-Portal
     */
    public void saveEoscPortalDomains(Set<EOSCPortalDomain> eoscPortalDomains){

        for(EOSCPortalDomain eoscPortalDomain : eoscPortalDomains){

            var domain = domainRepository.findByIdOptional(eoscPortalDomain.id);

            var eoscToDomain = DomainMapper.INSTANCE.eoscPortalDomainToDomain(eoscPortalDomain);

            if(domain.isPresent()){

                if(!domain.get().equals(eoscToDomain)){

                    domainRepository.updateById(eoscToDomain.name, eoscToDomain.description, eoscToDomain.id);
                }
            } else {
                domainRepository.persistAndFlush(eoscToDomain);
            }
        }

        LOG.info("EOSC-Portal domains have been successfully stored into the database.");
    }
}