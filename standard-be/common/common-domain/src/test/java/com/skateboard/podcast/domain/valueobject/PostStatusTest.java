package com.skateboard.podcast.domain.valueobject;

import com.skateboard.podcast.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PostStatusTest {

    @Test
    void parsesCaseInsensitiveValues() {
        assertEquals(PostStatus.DRAFT, PostStatus.from("draft"));
        assertEquals(PostStatus.PUBLISHED, PostStatus.from("PUBLISHED"));
    }

    @Test
    void rejectsBlankStatus() {
        assertThrows(ValidationException.class, () -> PostStatus.from(" "));
    }
}
