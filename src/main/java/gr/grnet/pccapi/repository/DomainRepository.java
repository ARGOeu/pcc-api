package gr.grnet.pccapi.repository;

import gr.grnet.pccapi.entity.Domain;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped

public class DomainRepository implements PanacheRepositoryBase<Domain, String> {

    /**
     * It updates the name and the description of the given domain id.
     *
     * @param name The domain name
     * @param description The domain description
     * @param id The domain id
     * @return whether the execution is successful or not
     */
    public long updateById(String name, String description, String id) {

        return update("#Domain.updateById", Parameters.with("name", name).and("description", description).and("id", id));
    }
}
