package gr.grnet.pccapi.repository;

import gr.grnet.pccapi.entity.Provider;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProviderRepository implements PanacheRepositoryBase<Provider, Integer> {}
