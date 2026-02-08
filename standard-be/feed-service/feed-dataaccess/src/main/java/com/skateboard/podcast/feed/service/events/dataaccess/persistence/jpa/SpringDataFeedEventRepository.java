package com.skateboard.podcast.feed.service.events.dataaccess.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataFeedEventRepository extends JpaRepository<FeedEventJpaEntity, UUID> {
    Page<FeedEventJpaEntity> findByStatusOrderByStartAtDesc(String status, Pageable pageable);
    Page<FeedEventJpaEntity> findByStatusOrderByUpdatedAtDesc(String status, Pageable pageable);
    Page<FeedEventJpaEntity> findAllByOrderByUpdatedAtDesc(Pageable pageable);
    Optional<FeedEventJpaEntity> findBySlug(String slug);
    long countByStatus(String status);

    @Query("select max(e.updatedAt) from FeedEventJpaEntity e where e.status = :status")
    Instant findMaxUpdatedAtByStatus(@Param("status") String status);
}

