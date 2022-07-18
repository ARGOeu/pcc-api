package gr.grnet.pccapi.service;

import gr.grnet.pccapi.dto.DomainDto;
import gr.grnet.pccapi.external.response.EOSCPortalDomain;
import gr.grnet.pccapi.mapper.DomainMapper;
import gr.grnet.pccapi.repository.DomainRepository;
import gr.grnet.pccapi.entity.Domain;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

import org.jboss.logging.Logger;

@ApplicationScoped
public class DomainService {

    DomainRepository domainRepository;

    Logger LOG;

    public DomainService(DomainRepository domainRepository, Logger LOG) {
        this.domainRepository = domainRepository;
        this.LOG = LOG;
    }


    @Transactional
    /**
     * It accepts the EOSC-Portal domains, turns them into {@link Domain domains} and then stores the transformed Domains into the database.
     * If the EOSC-Portal domain exists in the database, then updates its attributes.
     * @param eoscPortalDomains The {@link EOSCPortalDomain eoscPortalDomains} collected by EOSC-Portal
     */
    public void saveEoscPortalDomains(Set<EOSCPortalDomain> eoscPortalDomains){

        for(EOSCPortalDomain eoscPortalDomain : eoscPortalDomains){

            var domain = domainRepository.findByDomainId(eoscPortalDomain.domainId);

            var eoscToDomain = DomainMapper.INSTANCE.eoscPortalDomainToDomain(eoscPortalDomain);

            if(domain.isPresent()){

                if(!domain.get().equals(eoscToDomain)){

                    domainRepository.updateByDomainId(eoscToDomain.name, eoscToDomain.description, eoscToDomain.domainId);
                }
            } else {
                domainRepository.persistAndFlush(eoscToDomain);
            }
        }

        LOG.info("EOSC-Portal domains have been successfully stored into the database.");
    }

    /**
     * Returns a Domain by the given ID
     * @return The stored Domain has been turned into a response body.
     */
    public DomainDto getById(Integer id){

        LOG.infof("Fetching the Domain with ID : %s", id);

        var domain = domainRepository.findById(id);

        return DomainMapper.INSTANCE.domainToDto(domain);
    }

    /**
     * Returns the available API Scientific Domains
     * @return The stored Domains has been turned into a response body.
     */
    public List<DomainDto> getAll(){

        LOG.infof("Fetching all Domains.");

        var domains = domainRepository.findAll().list();

        return DomainMapper.INSTANCE.domainsToDto(domains);
    }
}