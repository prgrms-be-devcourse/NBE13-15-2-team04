package com.team04.playlist.playlist.service;

import com.team04.playlist.global.exception.BusinessException;
import com.team04.playlist.global.exception.ErrorCode;
import com.team04.playlist.playlist.repository.PlaylistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaylistLikeCounter {

    private final PlaylistRepository playlistRepository;

    // +1 후 좋아요 수 반환
    public int increaseLikeCount(Long playlistId) {
        playlistRepository.increaseLikeCount(playlistId);
        return currentLikeCount(playlistId);
    }

    // -1 후 좋아요 수 반환
    public int decreaseLikeCount(Long playlistId) {
        playlistRepository.decreaseLikeCount(playlistId);
        return currentLikeCount(playlistId);
    }

    // 플리 없거나 삭제됨 -> 404
    private int currentLikeCount(Long playlistId) {
        return playlistRepository.findLikeCount(playlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND));
    }
}
