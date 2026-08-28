package com.sift.modules.bookmark;

import com.sift.modules.collection.CollectionResponseDTO;
import com.sift.modules.tag.TagResponseDTO;
import com.sift.modules.note.NoteResponseDTO;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record BookmarkResponseDTO(
        UUID id,
        TweetResponseDTO tweet,
        CollectionResponseDTO collection,
        boolean isFavorite,
        boolean isRead,
        OffsetDateTime savedAt,
        List<TagResponseDTO> tags,
        NoteResponseDTO note
) {
}