package gr.grnet.pccapi.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.List;

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

  @ManyToOne()
  @JoinColumn(name = "contract_type_id")
  public Codelist contractType;

  @ManyToOne()
  @JoinColumn(name = "lookup_service_type_id")
  public Codelist lookUpServiceType;

  @OneToMany(mappedBy = "prefix")
  public List<Account> accounts;
}
