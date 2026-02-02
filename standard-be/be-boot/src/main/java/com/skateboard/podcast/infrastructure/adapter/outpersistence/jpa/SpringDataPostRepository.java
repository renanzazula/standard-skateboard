package com.skateboard.podcast.infrastructure.adapter.outpersistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataPostRepository extends JpaRepository<PostJpaEntity, UUID> {
    Page<PostJpaEntity> findByStatusOrderByPublishedAtDesc(String status, Pageable pageable);
    Optional<PostJpaEntity> findBySlug(String slug);
}
