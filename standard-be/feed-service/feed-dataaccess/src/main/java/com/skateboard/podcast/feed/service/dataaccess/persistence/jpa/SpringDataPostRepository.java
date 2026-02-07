package com.skateboard.podcast.feed.service.dataaccess.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataPostRepository extends JpaRepository<PostJpaEntity, UUID> {
    Page<PostJpaEntity> findByStatusOrderByPublishedAtDesc(String status, Pageable pageable);
    Page<PostJpaEntity> findByStatusOrderByUpdatedAtDesc(String status, Pageable pageable);
    Page<PostJpaEntity> findAllByOrderByUpdatedAtDesc(Pageable pageable);
    Optional<PostJpaEntity> findBySlug(String slug);
    long countByStatus(String status);

    @Query("select max(p.updatedAt) from PostJpaEntity p where p.status = :status")
    Instant findMaxUpdatedAtByStatus(@Param("status") String status);
}
