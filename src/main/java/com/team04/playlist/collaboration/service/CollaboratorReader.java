package com.team04.playlist.collaboration.service;

import com.team04.playlist.collaboration.repository.CollaboratorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 편집 권한을 갖고 있는지만 읽음
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollaboratorReader {

    private final CollaboratorRepository collaboratorRepository;

    public boolean isCollaborator(Long playlistId, Long userId) {
        return collaboratorRepository.existsByPlaylistIdAndUserId(playlistId, userId);
    }
}