package com.team04.playlist.playlist.dto;

import java.util.List;
import org.springframework.data.domain.Page;

// 공개 목록 응답의 data 부분. 명세서 3.2: content, page, size, totalElements, hasNext
public record PlaylistPageResponse(
        List<PlaylistSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        boolean hasNext
) {

    public static PlaylistPageResponse from(Page<PlaylistSummaryResponse> page) {
        return new PlaylistPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.hasNext());
    }
}