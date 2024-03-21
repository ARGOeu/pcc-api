package gr.grnet.pccapi.repository;

import gr.grnet.pccapi.entity.Codelist;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CodelistRepository implements PanacheRepositoryBase<Codelist, Integer> {

  public Optional<Codelist> findByIdAndCategory(Integer id, String category) {
    return find("from codelist c where c.id = ?1 and c.category = ?2", id, category)
        .firstResultOptional();
  }
}
