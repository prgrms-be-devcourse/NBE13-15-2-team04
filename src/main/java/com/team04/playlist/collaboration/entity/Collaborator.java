package com.team04.playlist.collaboration.entity;

import com.team04.playlist.playlist.entity.Playlist;
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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
        name = "collaborator",
        uniqueConstraints = @UniqueConstraint(name = "uk_collaborator", columnNames = {"playlist_id", "user_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Collaborator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by", nullable = false)
    private User invitedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Collaborator create(Playlist playlist, User user, User invitedBy) {
        Collaborator collaborator = new Collaborator();
        collaborator.playlist = playlist;
        collaborator.user = user;
        collaborator.invitedBy = invitedBy;
        return collaborator;
    }
}