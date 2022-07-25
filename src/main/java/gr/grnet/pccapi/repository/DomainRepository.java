package gr.grnet.pccapi.repository;

import gr.grnet.pccapi.entity.Domain;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DomainRepository implements PanacheRepositoryBase<Domain, Integer> {

  /**
   * It updates the name and the description of the given domain id.
   *
   * @param name The domain name
   * @param description The domain description
   * @param domainId The domain id
   * @return whether the execution is successful or not
   */
  public long updateByDomainId(String name, String description, String domainId) {

    return update(
        "#Domain.updateByDomainId",
        Parameters.with("name", name).and("description", description).and("domainId", domainId));
  }

  public Optional<Domain> findByDomainId(String domainId) {

    return find("#Domain.findByDomainId", domainId).firstResultOptional();
  }
}
