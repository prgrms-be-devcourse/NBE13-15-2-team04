package com.team04.playlist.playlist.dto;

import com.team04.playlist.global.exception.BusinessException;
import com.team04.playlist.global.exception.ErrorCode;
import org.springframework.data.domain.Sort;

// 최신순, 좋아요 순 정렬
public enum PlaylistSort {

    LATEST(Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))),
    LIKES(Sort.by(Sort.Order.desc("likeCount"), Sort.Order.desc("createdAt"), Sort.Order.desc("id")));

    private final Sort sort;

    PlaylistSort(Sort sort) {
        this.sort = sort;
    }

    public Sort toSort() {
        return sort;
    }

    public static PlaylistSort from(String value) {
        return switch (value) {
            case "latest" -> LATEST;
            case "likes" -> LIKES;
            default -> throw new BusinessException(ErrorCode.INVALID_INPUT);
        };
    }
}