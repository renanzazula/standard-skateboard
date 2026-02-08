package com.skateboard.podcast.event.service.dataaccess.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "events")
public class EventJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "slug", nullable = false, unique = true, length = 200)
    private String slug;

    @Column(name = "excerpt", length = 500)
    private String excerpt;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tags", nullable = false, columnDefinition = "text[]")
    private String[] tags;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "thumbnail", columnDefinition = "jsonb")
    private String thumbnailJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content", nullable = false, columnDefinition = "jsonb")
    private String contentJson;

    @Column(name = "start_at")
    private Instant startAt;

    @Column(name = "end_at")
    private Instant endAt;

    @Column(name = "timezone", length = 64)
    private String timezone;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "tickets_url", length = 500)
    private String ticketsUrl;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(final UUID id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(final String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(final String slug) { this.slug = slug; }

    public String getExcerpt() { return excerpt; }
    public void setExcerpt(final String excerpt) { this.excerpt = excerpt; }

    public String[] getTags() { return tags; }
    public void setTags(final String[] tags) { this.tags = tags; }

    public String getStatus() { return status; }
    public void setStatus(final String status) { this.status = status; }

    public String getThumbnailJson() { return thumbnailJson; }
    public void setThumbnailJson(final String thumbnailJson) { this.thumbnailJson = thumbnailJson; }

    public String getContentJson() { return contentJson; }
    public void setContentJson(final String contentJson) { this.contentJson = contentJson; }

    public Instant getStartAt() { return startAt; }
    public void setStartAt(final Instant startAt) { this.startAt = startAt; }

    public Instant getEndAt() { return endAt; }
    public void setEndAt(final Instant endAt) { this.endAt = endAt; }

    public String getTimezone() { return timezone; }
    public void setTimezone(final String timezone) { this.timezone = timezone; }

    public String getLocation() { return location; }
    public void setLocation(final String location) { this.location = location; }

    public String getTicketsUrl() { return ticketsUrl; }
    public void setTicketsUrl(final String ticketsUrl) { this.ticketsUrl = ticketsUrl; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(final UUID createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(final Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(final Instant updatedAt) { this.updatedAt = updatedAt; }
}
