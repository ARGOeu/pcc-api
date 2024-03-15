package gr.grnet.pccapi.repository;

import gr.grnet.pccapi.entity.Codelist;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CodelistRepository implements PanacheRepositoryBase<Codelist, Integer> {}
