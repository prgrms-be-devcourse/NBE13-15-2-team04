package com.team04.playlist.playlist.entity;

import com.team04.playlist.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "playlist")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 30)
    private String title;

    @Column(length = 200)
    private String description;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    // PlaylistLikeCounter로만 바꿈.
    @Column(name = "like_count", nullable = false, updatable = false)
    private int likeCount;

    @Column(name = "source_playlist_id")
    private Long sourcePlaylistId;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static Playlist create(User owner, String title, String description, boolean isPublic) {
        Playlist playlist = new Playlist();
        playlist.owner = owner;
        playlist.title = title;
        playlist.description = description;
        playlist.isPublic = isPublic;
        return playlist;
    }

    public void update(String title, String description, boolean isPublic) {
        this.title = title;
        this.description = description;
        this.isPublic = isPublic;
    }

    public void softDelete() {
        if (deletedAt == null) {
            deletedAt = LocalDateTime.now();
        }
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isOwnedBy(Long userId) {
        return owner.getId().equals(userId);
    }
}