package com.team04.playlist.like.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table (
        name = "playlist_like",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_like_user_playlist",
                columnNames = {"user_id", "playlist_id"}
        ),
        indexes = @Index(name = "idx_like_playlist",
                columnList = "playlist_id")
    )
public class PlaylistLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "playlist_id", nullable = false)
    private Long playlistId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private PlaylistLike(Long userId, Long playlistId) {
        this.userId = userId;
        this.playlistId = playlistId;
    }

    public static PlaylistLike of(Long userId, Long playlistId) {
        return new PlaylistLike(userId, playlistId);
    }
}
