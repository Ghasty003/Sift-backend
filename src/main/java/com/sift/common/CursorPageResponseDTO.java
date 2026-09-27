package com.sift.common;

import java.util.List;

public record CursorPageResponseDTO<T>(
        List<T> items,
        String nextCursor,
        boolean hasMore
) {
}