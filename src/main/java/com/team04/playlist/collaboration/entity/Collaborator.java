package com.team04.playlist.collaboration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @Column(name = "playlist_id", nullable = false)
    private Long playlistId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "invited_by", nullable = false)
    private Long invitedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Collaborator create(Long playlistId, Long userId, Long invitedBy) {
        Collaborator collaborator = new Collaborator();
        collaborator.playlistId = playlistId;
        collaborator.userId = userId;
        collaborator.invitedBy = invitedBy;
        return collaborator;
    }
}