package com.team04.playlist.playlist.service;

import com.team04.playlist.collaboration.service.CollaboratorReaderService;
import com.team04.playlist.global.exception.BusinessException;
import com.team04.playlist.global.exception.ErrorCode;
import com.team04.playlist.playlist.entity.Playlist;
import com.team04.playlist.playlist.entity.PlaylistRole;
import com.team04.playlist.playlist.repository.PlaylistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


// 검사 흐름 401 -> 404 -> 403 -> 403
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaylistAccessChecker {

    private final PlaylistRepository playlistRepository;
    private final CollaboratorReaderService collaboratorReaderService;

    // 조회, 비공개+비로그인 -> 401
    public Playlist getViewable(Long playlistId, Long userId) {
        Playlist playlist = findActive(playlistId);

        if (playlist.isPublic()) {
            return playlist;
        }
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        checkPrivateAccess(playlist, resolveRole(playlist, userId));
        return playlist;
    }

    // 수정(편집) 권한 조회, X -> 403
    public Playlist getEditable(Long playlistId, Long userId) {
        requireLogin(userId);
        Playlist playlist = findActive(playlistId);
        PlaylistRole role = resolveRole(playlist, userId);
        checkPrivateAccess(playlist, role);

        if (role == PlaylistRole.VIEWER) {
            throw new BusinessException(ErrorCode.NOT_PLAYLIST_EDITOR);
        }
        return playlist;
    }

    // 관리 권한 조회, X -> 403
    public Playlist getOwned(Long playlistId, Long userId) {
        requireLogin(userId);
        Playlist playlist = findActive(playlistId);
        PlaylistRole role = resolveRole(playlist, userId);
        checkPrivateAccess(playlist, role);

        if (role != PlaylistRole.OWNER) {
            throw new BusinessException(ErrorCode.NOT_PLAYLIST_OWNER);
        }
        return playlist;
    }

    // return 권한
    public PlaylistRole resolveRole(Playlist playlist, Long userId) {
        if (userId == null) {
            return PlaylistRole.VIEWER;
        }
        if (playlist.isOwnedBy(userId)) {
            return PlaylistRole.OWNER;
        }
        if (collaboratorReaderService.isCollaborator(playlist.getId(), userId)) {
            return PlaylistRole.COLLABORATOR;
        }
        return PlaylistRole.VIEWER;

    }

    // 로그인 유무 401
    private void requireLogin(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    // 플리 존재 유무, 없을경우 -> 404
    private Playlist findActive(Long playlistId) {
        return playlistRepository.findByIdAndDeletedAtIsNull(playlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND));
    }

    // 비공개인데 작업자 아닐경우 -> 403
    private void checkPrivateAccess(Playlist playlist, PlaylistRole role) {
        if (!playlist.isPublic() && role == PlaylistRole.VIEWER) {
            throw new BusinessException(ErrorCode.PRIVATE_PLAYLIST);
        }
    }




}
