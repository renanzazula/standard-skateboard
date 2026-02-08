package com.skateboard.podcast.event.service.dataaccess.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataEventRepository extends JpaRepository<EventJpaEntity, UUID> {
    Page<EventJpaEntity> findByStatusOrderByStartAtDesc(String status, Pageable pageable);
    Page<EventJpaEntity> findByStatusOrderByUpdatedAtDesc(String status, Pageable pageable);
    Page<EventJpaEntity> findAllByOrderByUpdatedAtDesc(Pageable pageable);
    Optional<EventJpaEntity> findBySlug(String slug);
    long countByStatus(String status);

    @Query("select max(e.updatedAt) from EventJpaEntity e where e.status = :status")
    Instant findMaxUpdatedAtByStatus(@Param("status") String status);
}
