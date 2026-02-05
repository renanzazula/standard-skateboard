package com.skateboard.podcast.domain.valueobject;

import com.skateboard.podcast.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TagTest {

    @Test
    void trimsTagValue() {
        final Tag tag = Tag.of("  Tech  ");
        assertEquals("Tech", tag.value());
    }

    @Test
    void rejectsBlankTag() {
        assertThrows(ValidationException.class, () -> Tag.of("   "));
    }
}
