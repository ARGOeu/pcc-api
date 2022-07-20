package gr.grnet.pccapi.repository;

import gr.grnet.pccapi.entity.Prefix;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PrefixRepository implements PanacheRepositoryBase<Prefix, Integer> {
}
