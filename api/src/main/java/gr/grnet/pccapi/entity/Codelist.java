package gr.grnet.pccapi.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.EqualsAndHashCode;

@Entity(name = "codelist")
public class Codelist extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Integer id;

  @EqualsAndHashCode.Include public String name;
  @EqualsAndHashCode.Include public String category;
}
