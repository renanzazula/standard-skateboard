package com.skateboard.podcast.appconfig.service.dataaccess.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "navigation_config")
public class NavigationConfigJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "config_json", nullable = false, columnDefinition = "TEXT")
    private String configJson;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    private void touchUpdatedAt() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(final Long id) { this.id = id; }

    public String getConfigJson() { return configJson; }
    public void setConfigJson(final String configJson) { this.configJson = configJson; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(final Instant updatedAt) { this.updatedAt = updatedAt; }
}
