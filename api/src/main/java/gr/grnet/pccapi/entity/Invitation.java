package gr.grnet.pccapi.entity;

import gr.grnet.pccapi.enums.InvitationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Entity
@Table(name = "invitation")
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String id;

    @NotNull
    @Column(name = "api_resource")
    public String apiResource;

    @NotNull
    @Column(name = "resource_id")
    public String resourceId;

    @NotNull
    @Column
    public String email;

    @NotNull
    @Column
    public String role;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column
    public InvitationStatus status = InvitationStatus.PENDING;

    @Column(name = "created_at")
    public Instant createdAt = Instant.now();

    @Column(name = "responded_at")
    public Instant respondedAt;

    @Column(name = "created_by")
    public String createdBy;

    @Column(name = "responded_by")
    public String respondedBy;

    @PrePersist
    public void prePersist() {

        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}