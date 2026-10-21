package gr.grnet.pccapi.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import java.util.HashSet;
import java.util.Set;

/**
 * A class representing a provider
 */
@Entity(name = "provider")
@DynamicUpdate
@Getter
@Setter
public class Provider extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String description;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "provider_contact",
            joinColumns = @JoinColumn(name = "provider_id"),
            inverseJoinColumns = @JoinColumn(name = "contact_id")
    )
    private Set<Contact> contacts = new HashSet<>();

    @OneToMany(mappedBy = "provider")
    private Set<PrefixRequest> prefixRequests = new HashSet<>();
}