package gr.grnet.pccapi.entity;

import gr.grnet.pccapi.enums.LookUpServiceType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicUpdate;

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

  @Enumerated(EnumType.STRING)
  @Column(name = "lookup_service_type", nullable = false)
  public LookUpServiceType lookUpServiceType;

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
