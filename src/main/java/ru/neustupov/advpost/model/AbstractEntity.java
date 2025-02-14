package ru.neustupov.advpost.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@MappedSuperclass
public abstract class AbstractEntity {

    @Id
    @SequenceGenerator(
            name = "adv_seq",
            sequenceName = "adv_sequence",
            allocationSize = 10,
            initialValue = 3000)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "adv_seq")
    @Column(name = "id", unique = true, nullable = false)
    protected Long id;
    @CreationTimestamp
    private LocalDateTime created;
    @UpdateTimestamp
    private LocalDateTime updated;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }
}
