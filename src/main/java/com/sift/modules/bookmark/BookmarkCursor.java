package com.sift.modules.bookmark;

import com.sift.exceptions.BadRequestException;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

record BookmarkCursor(OffsetDateTime savedAt, UUID id) {

    String encode() {
        String raw = savedAt.toString() + "|" + id;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    static BookmarkCursor decode(String cursor) {
        try {
            String raw = new String(
                    Base64.getUrlDecoder().decode(cursor),
                    StandardCharsets.UTF_8
            );
            String[] parts = raw.split("\\|", 2);
            return new BookmarkCursor(
                    OffsetDateTime.parse(parts[0]),
                    UUID.fromString(parts[1])
            );
        } catch (Exception e) {
            throw new BadRequestException("Invalid pagination cursor");
        }
    }
}