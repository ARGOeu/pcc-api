package gr.grnet.pccapi.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/** The entity class that represents a Service */
@Entity(name = "service")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
public class Service extends PanacheEntityBase {

  @EqualsAndHashCode.Include
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Integer id;

  @EqualsAndHashCode.Include public String name;
}
