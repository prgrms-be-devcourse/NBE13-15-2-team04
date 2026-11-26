package com.team04.playlist.like.dto;

import io.swagger.v3.oas.annotations.media.Schema;

//좋아요 등록(PUT) 응답. 취소(DELETE)는 204 No Content 라 응답 본문이 없다.
@Schema(description = "좋아요 등록 응답")
public record LikeResponse(

        @Schema(description = "플레이리스트 ID", example = "1")
        Long playlistId,

        @Schema(description = "요청 후 좋아요 상태", example = "true")
        boolean liked,

        @Schema(description = "요청 후 좋아요 수", example = "42")
        int likeCount
) {

    public static LikeResponse of(Long playlistId, boolean liked, int likeCount) {
        return new LikeResponse(playlistId, liked, likeCount);
    }
}
