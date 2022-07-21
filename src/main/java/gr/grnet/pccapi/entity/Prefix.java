package gr.grnet.pccapi.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Entity(name = "prefix")
@Setter
@DynamicUpdate
@Accessors(chain = true)
public class Prefix extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;
    public String name;
    public String owner;
    @Column(name = "used_by")
    public String usedBy;
    public Integer status;

    @ManyToOne()
    @JoinColumn(name = "domain_id")
    public Domain domain;

    @ManyToOne()
    @JoinColumn(name = "service_id")
    public Service service;

    @ManyToOne()
    @JoinColumn(name = "provider_id")
    public Provider provider;

}
