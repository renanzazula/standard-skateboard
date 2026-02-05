package com.skateboard.podcast.feed.service.application.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skateboard.podcast.feed.service.application.dto.PostDetailsView;
import com.skateboard.podcast.feed.service.application.dto.PostSummaryView;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PublicFeedControllerTest {

    private static final String CONTENT_JSON = "[{\"type\":\"paragraph\",\"text\":\"Hello\"}]";

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private PublicFeedUseCase publicFeedService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        final PostApiMapper postApiMapper = new PostApiMapper(objectMapper);
        final PublicFeedController controller = new PublicFeedController(publicFeedService, postApiMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void publicFeedList_returnsPage() throws Exception {
        final PostSummaryView summary = new PostSummaryView(
                UUID.randomUUID(),
                "First Post",
                "first-post",
                "Intro",
                List.of("news", "launch"),
                "PUBLISHED",
                objectMapper.writeValueAsString(new ImageRef().url("https://example.com/a.png").alt("thumb")),
                Instant.parse("2024-01-01T00:00:00Z")
        );

        given(publicFeedService.listPublished(1, 2)).willReturn(List.of(summary));

        mockMvc.perform(get("/public/feed")
                        .param("page", "1")
                        .param("size", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.items[0].slug").value("first-post"))
                .andExpect(jsonPath("$.items[0].thumbnail.url").value("https://example.com/a.png"));
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

        given(publicFeedService.getBySlug("hello-world")).willReturn(Optional.of(details));

        mockMvc.perform(get("/public/posts/hello-world")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("hello-world"))
                .andExpect(jsonPath("$.content[0].type").value("paragraph"))
                .andExpect(jsonPath("$.thumbnail.url").value("https://example.com/hero.png"));
    }

    @Test
    void publicPostGetBySlug_returnsNotFound() throws Exception {
        given(publicFeedService.getBySlug("missing")).willReturn(Optional.empty());

        mockMvc.perform(get("/public/posts/missing")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
