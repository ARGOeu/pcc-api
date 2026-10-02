package gr.grnet.pccapi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull
    @JoinColumn(name = "prefix_id")
    public Prefix prefix;

    @NotNull
    @Column
    public String email;

    @NotNull
    @Column
    public String endpoint;

    @NotNull
    @Column(name = "admin_index")
    public Integer adminIndex;

    @NotNull
    @Column
    public String permissions;

    @Column(name = "created_at")
    public Instant createdAt = Instant.now();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}