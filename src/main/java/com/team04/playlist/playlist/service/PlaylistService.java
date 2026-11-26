package com.team04.playlist.playlist.service;

import com.team04.playlist.global.exception.BusinessException;
import com.team04.playlist.global.exception.ErrorCode;
import com.team04.playlist.global.response.PageResponse;
import com.team04.playlist.playlist.dto.PlaylistCreateRequest;
import com.team04.playlist.playlist.dto.PlaylistResponse;
import com.team04.playlist.playlist.dto.PlaylistSort;
import com.team04.playlist.playlist.dto.PlaylistSummaryResponse;
import com.team04.playlist.playlist.dto.PlaylistUpdateRequest;
import com.team04.playlist.playlist.entity.Playlist;
import com.team04.playlist.playlist.repository.PlaylistRepository;
import com.team04.playlist.user.UserService;
import com.team04.playlist.user.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaylistService {

    private static final int MAX_PAGE_SIZE = 50;

    private final PlaylistRepository playlistRepository;
    private final PlaylistAccessChecker accessChecker;
    private final UserService userService;
    private final EntityManager entityManager;

    public PlaylistResponse create(Long userId, PlaylistCreateRequest request) {
        // 로그인 확인, 아니면 -> 401
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        // 유저가 있는지 확인, 없으면 -> 404 USER_NOT_FOUND
        userService.getUserSummary(userId);
        // 유저 엔티티 받아서 할당
        User owner = entityManager.getReference(User.class, userId);
        Playlist playlist = Playlist.create(owner, request.title(), request.description(), request.isPublicOrDefault());
        return PlaylistResponse.from(playlistRepository.save(playlist));
    }

    public PlaylistResponse update(Long playlistId, Long userId, PlaylistUpdateRequest request) {
        // 권한 검사기, PlaylistAccessChecker
        Playlist playlist = accessChecker.getOwned(playlistId, userId);
        playlist.update(
                orElse(request.title(), playlist.getTitle()),
                orElse(request.description(), playlist.getDescription()),
                orElse(request.isPublic(), playlist.isPublic()));
        return PlaylistResponse.from(playlist);
    }

    public void delete(Long playlistId, Long userId) {
        Playlist playlist = accessChecker.getOwned(playlistId, userId);
        playlist.softDelete();
    }

    // 플리 목록
    @Transactional(readOnly = true)
    public PageResponse<PlaylistSummaryResponse> getPublicPlaylists(PlaylistSort sort, int page, int size) {
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE), sort.toSort());
        return PageResponse.from(playlistRepository.findByIsPublicTrueAndDeletedAtIsNull(pageRequest)
                        .map(PlaylistSummaryResponse::from));
    }

    private static <T> T orElse(T value, T current) {
        return value != null ? value : current;
    }
}