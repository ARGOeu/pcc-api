package gr.grnet.pccapi.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries({
        @NamedQuery(name = "Domain.updateById", query = "update Domain d set d.name = :name, d.description = :description where d.id = :id")
})
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Domain extends PanacheEntityBase {

    @Id
    @EqualsAndHashCode.Include
    public String id;
    @EqualsAndHashCode.Include
    public String name;
    @EqualsAndHashCode.Include
    public String description;
}
