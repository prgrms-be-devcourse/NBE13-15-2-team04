package com.team04.playlist.playlist.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.team04.playlist.playlist.entity.Playlist;
import java.time.LocalDateTime;

// 목록 한 줄
public record PlaylistSummaryResponse(
        Long playlistId,
        Long ownerId,
        String ownerNickname,
        String title,
        int likeCount,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime createdAt
) {

    public static PlaylistSummaryResponse from(Playlist playlist) {
        return new PlaylistSummaryResponse(
                playlist.getId(),
                playlist.getOwner().getId(),
                playlist.getOwner().getNickname(),
                playlist.getTitle(),
                playlist.getLikeCount(),
                playlist.getCreatedAt());
    }
}