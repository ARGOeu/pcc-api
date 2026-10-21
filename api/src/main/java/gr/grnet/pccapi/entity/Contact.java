package gr.grnet.pccapi.entity;


import gr.grnet.pccapi.enums.ContactType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(
        name = "contact",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"contact_name", "contact_email", "contact_type"}
        )
)
@Getter
@Setter
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "contact_name")
    private String contactName;

    @NotNull
    @Column(name = "contact_email")
    private String contactEmail;


    @Convert(converter = ContactTypeConverter.class)
    @NotNull
    @Column(name = "contact_type")
    private ContactType contactType;

    @ManyToMany(mappedBy = "contacts")
    private java.util.Set<Provider> providers = new java.util.HashSet<>();

    // getters and setters
    public boolean equalsByInfo(Contact other) {
        return this.contactName.equals(other.contactName) && this.contactEmail.equals(other.contactEmail) && this.contactType.name().equals(other.contactType.name());
    }
}