package com.team04.playlist.library.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "library_playlist",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_library_playlist",
                columnNames = {"library_id", "playlist_id"}
        ),
        indexes = @Index(name = "idx_lp_playlist", columnList = "playlist_id")
)
public class LibraryPlaylist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "library_id", nullable = false)
    private Library library;

    @Column(name = "playlist_id", nullable = false)
    private Long playlistId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LibraryPlaylist(Library library, Long playlistId) {
        this.library = library;
        this.playlistId = playlistId;
    }

    public static LibraryPlaylist of(Library library, Long playlistId) {
        return new LibraryPlaylist(library, playlistId);
    }
}
