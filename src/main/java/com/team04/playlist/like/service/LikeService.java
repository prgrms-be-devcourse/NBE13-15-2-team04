package com.team04.playlist.like.service;

import com.team04.playlist.global.exception.BusinessException;
import com.team04.playlist.global.exception.ErrorCode;
import com.team04.playlist.like.dto.LikeResponse;
import com.team04.playlist.like.repository.PlaylistLikeRepository;
import com.team04.playlist.playlist.entity.Playlist;
import com.team04.playlist.playlist.service.PlaylistAccessChecker;
import com.team04.playlist.playlist.service.PlaylistLikeCounter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeService {

    private final PlaylistLikeRepository playlistLikeRepository;
    private final PlaylistAccessChecker playlistAccessChecker;
    private final PlaylistLikeCounter playlistLikeCounter;

    //좋아요 등록: 401 → 404 → 403 → 등록
    @Transactional
    public LikeResponse like(Long userId, Long playlistId) {
        requireLogin(userId);
        Playlist playlist = playlistAccessChecker.getViewable(playlistId, userId);

        int inserted = playlistLikeRepository.insertIgnore(userId, playlistId);
        if (inserted == 1) {
            int likeCount = playlistLikeCounter.increaseLikeCount(playlistId);
            return LikeResponse.of(playlistId, true, likeCount);
        }

        //이미 좋아요 상태
        return LikeResponse.of(playlistId, true, playlist.getLikeCount());
    }

    //좋아요 취소 : 로그인만 확인. 비공개로 바뀌었거나 삭제된 플레이리스트도 취소는 가능해야 한다.
    @Transactional
    public void unlike(Long userId, Long playlistId) {
        requireLogin(userId);

        int deleted = playlistLikeRepository.deleteByUserIdAndPlaylistId(userId, playlistId);
        if (deleted == 1) {
            //실제로 지워졌을 때만 -1
            playlistLikeCounter.decreaseLikeCount(playlistId);
        }
    }

    //플레이리스트 상세 조회용
    public boolean isLiked(Long userId, Long playlistId) {
        if (userId == null) {
            return false;
        }
        return playlistLikeRepository.existsByUserIdAndPlaylistId(userId, playlistId);
    }

    private void requireLogin(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
}
