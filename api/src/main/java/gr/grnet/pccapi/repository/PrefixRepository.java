package gr.grnet.pccapi.repository;

import gr.grnet.pccapi.entity.Prefix;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PrefixRepository implements PanacheRepositoryBase<Prefix, Integer> {

  /**
   * Checks if the given prefix name has already been used
   *
   * @param name of the prefix
   * @return true or false
   */
  public boolean existsByName(String name) {
    return find("name", name).count() >= 1L;
  }
}
