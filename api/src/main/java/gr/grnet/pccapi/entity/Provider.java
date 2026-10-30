package gr.grnet.pccapi.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import java.util.HashSet;
import java.util.List;
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

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "logo")
    private String logo;

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