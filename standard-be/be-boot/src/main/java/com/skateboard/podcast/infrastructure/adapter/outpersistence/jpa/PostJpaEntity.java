package com.skateboard.podcast.infrastructure.adapter.outpersistence.jpa;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "posts")
public class PostJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "slug", nullable = false, unique = true, length = 200)
    private String slug;

    @Column(name = "excerpt", length = 500)
    private String excerpt;

    @Column(name = "tags", nullable = false, columnDefinition = "text[]")
    private String[] tags;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "thumbnail", columnDefinition = "jsonb")
    private String thumbnailJson;

    @Column(name = "content", nullable = false, columnDefinition = "jsonb")
    private String contentJson;

    @Column(name = "author_id")
    private UUID authorId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "published_at")
    private Instant publishedAt;

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

    public UUID getAuthorId() { return authorId; }
    public void setAuthorId(final UUID authorId) { this.authorId = authorId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(final Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(final Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(final Instant publishedAt) { this.publishedAt = publishedAt; }
}
