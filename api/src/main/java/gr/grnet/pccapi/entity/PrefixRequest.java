package gr.grnet.pccapi.entity;

import gr.grnet.pccapi.entity.Provider;
import gr.grnet.pccapi.enums.PrefixExpectedHandles;
import gr.grnet.pccapi.enums.PrefixRequestStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import java.time.Instant;


import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * A class representing a prefix request submitted by a user.
 */
@Entity
@Table(name = "prefix_request")
@DynamicUpdate
@Getter
@Setter
public class PrefixRequest extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "contact_person_name", nullable = false)
    private String contactPersonName;

    @Column(name = "contact_person_surname", nullable = false)
    private String contactPersonSurname;

    @Column(name = "contact_person_email", nullable = false)
    private String contactPersonEmail;

    @Column(name = "contact_person_phone")
    private String contactPersonPhone;

    private String country;

    @Column(name = "organization_name", nullable = false)
    private String organizationName;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "project_url", length = 2048)
    private String projectUrl;

    @Column(name = "project_description", columnDefinition = "TEXT")
    private String projectDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "expected_handles", length = 50)
    private PrefixExpectedHandles expectedHandles;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrefixRequestStatus status = PrefixRequestStatus.PENDING;

    @Column(name = "status_reason", columnDefinition = "TEXT")
    private String statusReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private Provider provider;


    @Column(name = "created_at")
    public Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    public Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
