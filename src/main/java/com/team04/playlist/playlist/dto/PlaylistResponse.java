package com.team04.playlist.playlist.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.team04.playlist.playlist.entity.Playlist;
import java.time.LocalDateTime;

public record PlaylistResponse(
        Long playlistId,
        Long ownerId,
        String ownerNickname,
        String title,
        String description,
        boolean isPublic,
        int likeCount,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime createdAt
) {

    public static PlaylistResponse from(Playlist playlist) {
        return new PlaylistResponse(
                playlist.getId(),
                playlist.getOwner().getId(),
                playlist.getOwner().getNickname(),
                playlist.getTitle(),
                playlist.getDescription(),
                playlist.isPublic(),
                playlist.getLikeCount(),
                playlist.getCreatedAt());
    }
}