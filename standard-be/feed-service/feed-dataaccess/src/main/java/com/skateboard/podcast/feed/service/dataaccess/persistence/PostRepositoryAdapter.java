package com.skateboard.podcast.feed.service.dataaccess.persistence;

import com.skateboard.podcast.feed.service.application.port.out.PostRepository;
import com.skateboard.podcast.feed.service.dataaccess.persistence.jpa.PostJpaEntity;
import com.skateboard.podcast.feed.service.dataaccess.persistence.jpa.SpringDataPostRepository;
import com.skateboard.podcast.domain.valueobject.PostStatus;
import com.skateboard.podcast.domain.valueobject.Slug;
import com.skateboard.podcast.domain.valueobject.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class PostRepositoryAdapter implements PostRepository {

    private final SpringDataPostRepository repo;

    public PostRepositoryAdapter(final SpringDataPostRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<PostRecord> findBySlug(final Slug slug) {
        return repo.findBySlug(slug.value()).map(PostRepositoryAdapter::toRecord);
    }

    @Override
    public Optional<PostRecord> findById(final java.util.UUID id) {
        return repo.findById(id).map(PostRepositoryAdapter::toRecord);
    }

    @Override
    public List<PostRecord> findPublished(final int page, final int size) {
        return repo.findByStatusOrderByPublishedAtDesc(
                        PostStatus.PUBLISHED.name(),
                        PageRequest.of(page, size)
                )
                .map(PostRepositoryAdapter::toRecord)
                .getContent();
    }

    @Override
    public PostRecord save(final PostRecord post) {
        final PostJpaEntity e = new PostJpaEntity();
        e.setId(post.id());
        e.setTitle(post.title());
        e.setSlug(post.slug().value());
        e.setExcerpt(post.excerpt());
        e.setTags(toTagArray(post.tags()));
        e.setStatus(post.status().name());
        e.setThumbnailJson(post.thumbnailJson());
        e.setContentJson(post.contentJson());
        e.setAuthorId(post.authorId());
        e.setCreatedAt(post.createdAt());
        e.setUpdatedAt(post.updatedAt());
        e.setPublishedAt(post.publishedAt());
        repo.save(e);
        return post;
    }

    private static PostRecord toRecord(final PostJpaEntity e) {
        return new PostRecord(
                e.getId(),
                e.getTitle(),
                Slug.of(e.getSlug()),
                e.getExcerpt(),
                toTags(e.getTags()),
                PostStatus.from(e.getStatus()),
                e.getThumbnailJson(),
                e.getContentJson(),
                e.getAuthorId(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getPublishedAt()
        );
    }

    private static List<Tag> toTags(final String[] tags) {
        if (tags == null || tags.length == 0) {
            return List.of();
        }
        return Arrays.stream(tags)
                .map(Tag::of)
                .toList();
    }

    private static String[] toTagArray(final List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return new String[0];
        }
        return tags.stream()
                .map(Tag::value)
                .toArray(String[]::new);
    }
}
