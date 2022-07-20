package gr.grnet.pccapi.service;

import gr.grnet.pccapi.dto.PrefixDto;
import gr.grnet.pccapi.entity.Domain;
import gr.grnet.pccapi.entity.Prefix;
import gr.grnet.pccapi.entity.Provider;
import gr.grnet.pccapi.entity.Service;
import gr.grnet.pccapi.mapper.PrefixMapper;
import gr.grnet.pccapi.repository.DomainRepository;
import gr.grnet.pccapi.repository.PrefixRepository;
import gr.grnet.pccapi.repository.ProviderRepository;
import gr.grnet.pccapi.repository.ServiceRepository;
import io.quarkus.logging.Log;
import lombok.AllArgsConstructor;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

@ApplicationScoped
@AllArgsConstructor
public class PrefixService {

     DomainRepository domainRepository;
     ProviderRepository providerRepository;
     ServiceRepository serviceRepository;
     PrefixRepository prefixRepository;
     Logger log;

     @Transactional
    public PrefixDto create(PrefixDto prefixDto) {

         log.info("Inserting new prefix . . .");

        // check the existence of the provided service
        Service service = serviceRepository.findByIdOptional(prefixDto.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        // check the existence of the provided domain
        Domain domain = domainRepository.findByIdOptional(prefixDto.getDomainId())
                .orElseThrow(() -> new RuntimeException("Domain not found"));

        // check the existence of the provided provider
        Provider provider = providerRepository.findByIdOptional(prefixDto.getProviderId())
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        Prefix prefix = new Prefix()
                .setService(service)
                .setDomain(domain)
                .setProvider(provider)
                .setOwner(prefixDto.getOwner())
                .setName(prefixDto.getName())
                .setUsedBy(prefixDto.getUsedBy())
                .setStatus(prefixDto.getStatus());

        prefixRepository.persist(prefix);

       return PrefixMapper.INSTANCE.prefixToDto(prefix);
    }
}
