package gr.grnet.pccapi.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity(name = "domain")
@NamedQueries({
        @NamedQuery(name = "Domain.updateByDomainId", query = "update domain d set d.name = :name, d.description = :description where d.domainId = :domainId"),
        @NamedQuery(name = "Domain.findByDomainId", query = "from domain where domainId = ?1")
})
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
/**
 * Basically, the Domain entity represents the Domain table in the API's database, and each entity instance corresponds to a row in that table.
 * The Domain entity represents an EOSC-Portal Scientific Domain.
 */
public class Domain extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;
    @EqualsAndHashCode.Include
    @Column(name = "domain_id")
    public String domainId;
    @EqualsAndHashCode.Include
    public String name;
    @EqualsAndHashCode.Include
    public String description;
}
