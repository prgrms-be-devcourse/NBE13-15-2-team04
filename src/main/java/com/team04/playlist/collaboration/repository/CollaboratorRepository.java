package com.team04.playlist.collaboration.repository;

import com.team04.playlist.collaboration.entity.Collaborator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollaboratorRepository extends JpaRepository<Collaborator, Long> {

    boolean existsByPlaylistIdAndUserId(Long playlistId, Long userId);
}