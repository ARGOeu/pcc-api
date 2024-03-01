package gr.grnet.pccapi.entity;

import gr.grnet.pccapi.enums.ContractType;
import gr.grnet.pccapi.enums.LookUpServiceType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.sql.Timestamp;
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

  @Column(name = "contact_name")
  public String contactName;

  @Column(name = "contact_email")
  public String contactEmail;

  @Column(name = "contract_end")
  public Timestamp contractEnd;

  @Enumerated(EnumType.STRING)
  @Column(name = "lookup_service_type", columnDefinition = "enum", nullable = false)
  public LookUpServiceType lookUpServiceType;

  public Integer status;

  @Enumerated(EnumType.STRING)
  @Column(name = "contract_type", columnDefinition = "enum")
  public ContractType contractType;

  @ManyToOne()
  @JoinColumn(name = "domain_id")
  public Domain domain;

  @ManyToOne()
  @JoinColumn(name = "service_id")
  public Service service;

  @ManyToOne()
  @JoinColumn(name = "provider_id")
  public Provider provider;

  public Boolean resolvable;
}
