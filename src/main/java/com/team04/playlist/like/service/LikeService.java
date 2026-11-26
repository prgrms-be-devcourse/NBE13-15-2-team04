package com.team04.playlist.like.service;

import com.team04.playlist.global.exception.BusinessException;
import com.team04.playlist.global.exception.ErrorCode;
import com.team04.playlist.like.dto.LikeResponse;
import com.team04.playlist.like.repository.PlaylistLikeRepository;
import com.team04.playlist.playlist.service.PlaylistAccessChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeService {

    private final PlaylistLikeRepository playlistLikeRepository;
    //플레이리스트 접근 검사를 맡는 playlist 쪽 공통 검사기
    private final PlaylistAccessChecker playlistAccessChecker;

    //좋아요 등록: 401 → 404 → 403 → 등록
    @Transactional
    public LikeResponse like(Long userId, Long playlistId) {
        requireLogin(userId);
        playlistAccessChecker.getViewable(playlistId, userId);

        int inserted = playlistLikeRepository.insertIgnore(userId, playlistId);
        if (inserted == 1) {
            // TODO(김지원님 요청): playlist.like_count +1 (UPDATE ... SET like_count = like_count + 1)
        }
        // TODO: 증감 메서드 반환값(증감 후 like_count)으로 교체. 임시로 0
        return LikeResponse.of(playlistId, true, 0);
    }

    //좋아요 취소 : 로그인만 확인. 비공개로 바뀌었거나 삭제된 플레이리스트도 취소는 가능해야 한다.
    @Transactional
    public void unlike(Long userId, Long playlistId) {
        requireLogin(userId);

        int deleted = playlistLikeRepository.deleteByUserIdAndPlaylistId(userId, playlistId);
        if (deleted == 1) {
            // TODO(김지원님 요청): playlist.like_count -1 (UPDATE ... SET like_count = like_count - 1 WHERE like_count > 0)
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
