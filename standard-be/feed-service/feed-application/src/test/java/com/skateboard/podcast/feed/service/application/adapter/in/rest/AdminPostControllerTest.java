package com.skateboard.podcast.feed.service.application.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skateboard.podcast.domain.security.CurrentUser;
import com.skateboard.podcast.feed.service.application.dto.PostDetailsView;
import com.skateboard.podcast.feed.service.application.port.in.AdminPostUseCase;
import com.skateboard.podcast.standardbe.api.model.BlockType;
import com.skateboard.podcast.standardbe.api.model.CreatePostRequest;
import com.skateboard.podcast.standardbe.api.model.ImageRef;
import com.skateboard.podcast.standardbe.api.model.ParagraphBlock;
import com.skateboard.podcast.standardbe.api.model.UpdatePostRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminPostControllerTest {

    private static final String CONTENT_JSON = "[{\"type\":\"paragraph\",\"text\":\"Hello\"}]";

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private AdminPostUseCase adminPostService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        final PostApiMapper postApiMapper = new PostApiMapper(objectMapper);
        final AdminPostController controller = new AdminPostController(adminPostService, postApiMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void adminPostCreate_returnsCreatedPost() throws Exception {
        final UUID authorId = UUID.randomUUID();
        authenticate(authorId);

        final CreatePostRequest request = new CreatePostRequest()
                .title("Draft Title")
                .slug("draft-title")
                .excerpt("Excerpt")
                .tags(List.of("news"))
                .thumbnail(new ImageRef().url("https://example.com/thumb.png").alt("thumb"))
                .content(List.of(new ParagraphBlock().type(BlockType.PARAGRAPH).text("Hello")));

        final UUID postId = UUID.randomUUID();
        final PostDetailsView view = new PostDetailsView(
                postId,
                "Draft Title",
                "draft-title",
                "Excerpt",
                List.of("news"),
                "DRAFT",
                objectMapper.writeValueAsString(new ImageRef().url("https://example.com/thumb.png").alt("thumb")),
                CONTENT_JSON,
                authorId,
                Instant.parse("2024-01-03T00:00:00Z")
        );

        given(adminPostService.createDraft(
                eq("Draft Title"),
                eq("draft-title"),
                eq("Excerpt"),
                eq(List.of("news")),
                anyString(),
                anyString(),
                eq(authorId)
        )).willReturn(view);

        mockMvc.perform(post("/admin/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(postId.toString()))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void adminPostUpdate_returnsUpdatedPost() throws Exception {
        final UUID postId = UUID.randomUUID();
        final UpdatePostRequest request = new UpdatePostRequest()
                .title("Updated")
                .slug("updated")
                .excerpt("New excerpt")
                .tags(List.of("editorial"))
                .thumbnail(new ImageRef().url("https://example.com/new.png").alt("new"))
                .content(List.of(new ParagraphBlock().type(BlockType.PARAGRAPH).text("Updated")));

        final PostDetailsView view = new PostDetailsView(
                postId,
                "Updated",
                "updated",
                "New excerpt",
                List.of("editorial"),
                "DRAFT",
                objectMapper.writeValueAsString(new ImageRef().url("https://example.com/new.png").alt("new")),
                CONTENT_JSON,
                UUID.randomUUID(),
                Instant.parse("2024-01-04T00:00:00Z")
        );

        given(adminPostService.updateById(
                eq(postId),
                eq("Updated"),
                eq("updated"),
                eq("New excerpt"),
                eq(List.of("editorial")),
                anyString(),
                anyString()
        )).willReturn(view);

        mockMvc.perform(put("/admin/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId.toString()))
                .andExpect(jsonPath("$.slug").value("updated"));
    }

    @Test
    void adminPostPublish_returnsPublishedPost() throws Exception {
        final UUID postId = UUID.randomUUID();
        final PostDetailsView view = new PostDetailsView(
                postId,
                "Published",
                "published",
                "Excerpt",
                List.of("news"),
                "PUBLISHED",
                null,
                CONTENT_JSON,
                UUID.randomUUID(),
                Instant.parse("2024-01-05T00:00:00Z")
        );

        given(adminPostService.publishById(postId)).willReturn(view);

        mockMvc.perform(post("/admin/posts/{id}/publish", postId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    private static void authenticate(final UUID userId) {
        final CurrentUser principal = new CurrentUser(userId, "ADMIN", "admin@example.com");
        final var authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
