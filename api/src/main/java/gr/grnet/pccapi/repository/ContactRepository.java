package gr.grnet.pccapi.repository;

import gr.grnet.pccapi.entity.Contact;
import gr.grnet.pccapi.enums.ContactType;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

import static io.quarkus.hibernate.orm.panache.PanacheEntityBase.find;

@ApplicationScoped
public class ContactRepository implements PanacheRepository<Contact> {

    public Optional<Contact> findByNameEmailAndType(
            String name, String email, ContactType type) {

        return find(
                "contactName = ?1 and contactEmail = ?2 and contactType = ?3",
                name, email, type
        ).firstResultOptional();
    }
}