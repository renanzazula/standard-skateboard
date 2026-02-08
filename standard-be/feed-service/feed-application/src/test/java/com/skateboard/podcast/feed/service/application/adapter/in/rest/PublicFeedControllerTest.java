package com.skateboard.podcast.feed.service.application.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skateboard.podcast.feed.service.application.dto.FeedItemSummaryView;
import com.skateboard.podcast.feed.service.application.dto.FeedVersion;
import com.skateboard.podcast.feed.service.application.dto.PostDetailsView;
import com.skateboard.podcast.feed.service.application.port.in.PublicPostsUseCase;
import com.skateboard.podcast.feed.service.application.port.in.PublicFeedUseCase;
import com.skateboard.podcast.standardbe.api.model.ImageRef;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.RequestContextFilter;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PublicFeedControllerTest {

    private static final String CONTENT_JSON = "[{\"type\":\"paragraph\",\"text\":\"Hello\"}]";

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private PublicFeedUseCase publicFeedUseCase;

    @Mock
    private PublicPostsUseCase publicPostsUseCase;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        final PostApiMapper postApiMapper = new PostApiMapper(objectMapper);
        final FeedApiMapper feedApiMapper = new FeedApiMapper(objectMapper);
        final PublicFeedController controller = new PublicFeedController(
                publicFeedUseCase,
                publicPostsUseCase,
                feedApiMapper,
                postApiMapper
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addFilters(new RequestContextFilter())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void publicFeedList_returnsPage() throws Exception {
        final FeedItemSummaryView summary = new FeedItemSummaryView(
                "POST",
                UUID.randomUUID(),
                "First Post",
                "first-post",
                "Intro",
                List.of("news", "launch"),
                "PUBLISHED",
                objectMapper.writeValueAsString(new ImageRef().url("https://example.com/a.png").alt("thumb")),
                Instant.parse("2024-01-01T00:00:00Z"),
                null,
                null,
                null,
                null,
                null,
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                UUID.randomUUID()
        );

        final FeedVersion version = new FeedVersion(
                "etag-1",
                Instant.parse("2024-01-02T00:00:00Z")
        );

        given(publicFeedUseCase.listPublished(1, 2)).willReturn(List.of(summary));
        given(publicFeedUseCase.getFeedVersion(1, 2)).willReturn(version);

        mockMvc.perform(get("/public/feed")
                        .param("page", "1")
                        .param("size", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("ETag", "\"etag-1\""))
                .andExpect(header().exists("Last-Modified"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.items[0].type").value("POST"))
                .andExpect(jsonPath("$.items[0].slug").value("first-post"))
                .andExpect(jsonPath("$.items[0].thumbnail.url").value("https://example.com/a.png"));
    }

    @Test
    void publicFeedList_returnsNotModifiedWhenEtagMatches() throws Exception {
        final FeedVersion version = new FeedVersion(
                "etag-2",
                Instant.parse("2024-01-03T00:00:00Z")
        );

        given(publicFeedUseCase.getFeedVersion(0, 20)).willReturn(version);

        mockMvc.perform(get("/public/feed")
                        .header("If-None-Match", "\"etag-2\"")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotModified())
                .andExpect(header().string("ETag", "\"etag-2\""))
                .andExpect(header().exists("Last-Modified"));
    }

    @Test
    void publicFeedList_returnsNotModifiedWhenLastModifiedSince() throws Exception {
        final Instant updatedAt = Instant.parse("2024-01-04T00:00:00Z");
        final FeedVersion version = new FeedVersion("etag-3", updatedAt);
        final String ifModifiedSince = DateTimeFormatter.RFC_1123_DATE_TIME
                .withZone(ZoneOffset.UTC)
                .format(updatedAt);

        given(publicFeedUseCase.getFeedVersion(0, 20)).willReturn(version);

        mockMvc.perform(get("/public/feed")
                        .header("If-Modified-Since", ifModifiedSince)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotModified())
                .andExpect(header().string("ETag", "\"etag-3\""))
                .andExpect(header().exists("Last-Modified"));
    }

    @Test
    void publicFeedList_prioritizesIfNoneMatchOverIfModifiedSince() throws Exception {
        final Instant updatedAt = Instant.parse("2024-01-05T00:00:00Z");
        final FeedVersion version = new FeedVersion("etag-4", updatedAt);
        final String ifModifiedSince = DateTimeFormatter.RFC_1123_DATE_TIME
                .withZone(ZoneOffset.UTC)
                .format(updatedAt);

        final FeedItemSummaryView summary = new FeedItemSummaryView(
                "POST",
                UUID.randomUUID(),
                "Second Post",
                "second-post",
                "Intro",
                List.of("news"),
                "PUBLISHED",
                objectMapper.writeValueAsString(new ImageRef().url("https://example.com/b.png").alt("thumb")),
                Instant.parse("2024-01-01T00:00:00Z"),
                null,
                null,
                null,
                null,
                null,
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                UUID.randomUUID()
        );

        given(publicFeedUseCase.getFeedVersion(0, 20)).willReturn(version);
        given(publicFeedUseCase.listPublished(0, 20)).willReturn(List.of(summary));

        mockMvc.perform(get("/public/feed")
                        .header("If-None-Match", "\"different\"")
                        .header("If-Modified-Since", ifModifiedSince)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("ETag", "\"etag-4\""))
                .andExpect(header().exists("Last-Modified"));
    }

    @Test
    void publicPostGetBySlug_returnsDetails() throws Exception {
        final String thumbnailJson = objectMapper.writeValueAsString(
                new ImageRef().url("https://example.com/hero.png").alt("hero")
        );
        final PostDetailsView details = new PostDetailsView(
                UUID.randomUUID(),
                "Hello",
                "hello-world",
                "Excerpt",
                List.of("feature"),
                "PUBLISHED",
                thumbnailJson,
                CONTENT_JSON,
                UUID.randomUUID(),
                Instant.parse("2024-01-02T00:00:00Z")
        );

        given(publicPostsUseCase.getBySlug("hello-world")).willReturn(Optional.of(details));

        mockMvc.perform(get("/public/posts/hello-world")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("hello-world"))
                .andExpect(jsonPath("$.content[0].type").value("paragraph"))
                .andExpect(jsonPath("$.thumbnail.url").value("https://example.com/hero.png"));
    }

    @Test
    void publicPostGetBySlug_returnsNotFound() throws Exception {
        given(publicPostsUseCase.getBySlug("missing")).willReturn(Optional.empty());

        mockMvc.perform(get("/public/posts/missing")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}

