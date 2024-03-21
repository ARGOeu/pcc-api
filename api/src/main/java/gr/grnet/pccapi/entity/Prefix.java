package gr.grnet.pccapi.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
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

  @Column(name = "contact_name")
  public String contactName;

  @Column(name = "contact_email")
  public String contactEmail;

  @Column(name = "contract_end")
  public Timestamp contractEnd;

  public Integer status;

  //  @Enumerated(EnumType.STRING)
  //  @Column(name = "contract_type", columnDefinition = "enum")
  //  public ContractType contractType;

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
  //
  //  @Enumerated(EnumType.STRING)
  //  @Column(name = "lookup_service_type", columnDefinition = "enum")
  //  public LookUpServiceType lookUpServiceType;
  //
  //  @ManyToOne()
  //  @JoinColumn(name = "lookup_service_type")
  //  public Codelist lookupServiceType;

  @ManyToOne()
  @JoinColumn(name = "contract_type_id")
  public Codelist contractType;

  @ManyToOne()
  @JoinColumn(name = "lookup_service_type_id")
  public Codelist lookUpServiceType;
}
