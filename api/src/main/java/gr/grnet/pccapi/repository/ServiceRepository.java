package gr.grnet.pccapi.repository;

import gr.grnet.pccapi.entity.Service;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import javax.enterprise.context.ApplicationScoped;

/** Repository class for the service entity */
@ApplicationScoped
public class ServiceRepository implements PanacheRepositoryBase<Service, Integer> {}
