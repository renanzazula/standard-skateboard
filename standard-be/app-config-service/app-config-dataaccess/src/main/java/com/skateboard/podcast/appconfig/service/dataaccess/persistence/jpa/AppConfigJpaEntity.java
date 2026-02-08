package com.skateboard.podcast.appconfig.service.dataaccess.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "app_config")
public class AppConfigJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "social_login_enabled", nullable = false)
    private Boolean socialLoginEnabled;

    @Column(name = "posts_per_page", nullable = false)
    private Integer postsPerPage;

    @Column(name = "splash_enabled", nullable = false)
    private Boolean splashEnabled;

    @Column(name = "splash_media_type", nullable = false, length = 20)
    private String splashMediaType;

    @Column(name = "splash_media_url", length = 500)
    private String splashMediaUrl;

    @Column(name = "splash_duration", nullable = false)
    private Integer splashDuration;

    @Column(name = "splash_show_close_button", nullable = false)
    private Boolean splashShowCloseButton;

    @Column(name = "splash_close_button_delay", nullable = false)
    private Integer splashCloseButtonDelay;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    private void touchUpdatedAt() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(final Long id) { this.id = id; }

    public Boolean getSocialLoginEnabled() { return socialLoginEnabled; }
    public void setSocialLoginEnabled(final Boolean socialLoginEnabled) { this.socialLoginEnabled = socialLoginEnabled; }

    public Integer getPostsPerPage() { return postsPerPage; }
    public void setPostsPerPage(final Integer postsPerPage) { this.postsPerPage = postsPerPage; }

    public Boolean getSplashEnabled() { return splashEnabled; }
    public void setSplashEnabled(final Boolean splashEnabled) { this.splashEnabled = splashEnabled; }

    public String getSplashMediaType() { return splashMediaType; }
    public void setSplashMediaType(final String splashMediaType) { this.splashMediaType = splashMediaType; }

    public String getSplashMediaUrl() { return splashMediaUrl; }
    public void setSplashMediaUrl(final String splashMediaUrl) { this.splashMediaUrl = splashMediaUrl; }

    public Integer getSplashDuration() { return splashDuration; }
    public void setSplashDuration(final Integer splashDuration) { this.splashDuration = splashDuration; }

    public Boolean getSplashShowCloseButton() { return splashShowCloseButton; }
    public void setSplashShowCloseButton(final Boolean splashShowCloseButton) { this.splashShowCloseButton = splashShowCloseButton; }

    public Integer getSplashCloseButtonDelay() { return splashCloseButtonDelay; }
    public void setSplashCloseButtonDelay(final Integer splashCloseButtonDelay) { this.splashCloseButtonDelay = splashCloseButtonDelay; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(final Instant updatedAt) { this.updatedAt = updatedAt; }
}
