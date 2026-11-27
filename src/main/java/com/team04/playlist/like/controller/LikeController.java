package com.team04.playlist.like.controller;

import com.team04.playlist.global.exception.ErrorResponse;
import com.team04.playlist.like.dto.LikeResponse;
import com.team04.playlist.like.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "좋아요")
@RestController
@RequestMapping("/api/v1/playlists/{playlistId}/like")
@RequiredArgsConstructor
//userId: JwtAuthFilter 가 토큰 subject(userId 문자열)를 principal 로 넣는다.
//좋아요 API 는 SecurityConfig 에서 로그인 필수라 항상 숫자 문자열이 들어온다. (비로그인 허용으로 바꾸면 변환 처리 필요)
public class LikeController {

    private final LikeService likeService;

    @Operation(
            summary = "좋아요",
            description = "로그인 필요. 공개 플레이리스트(비공개면 OWNER·COLLABORATOR)만 가능합니다. "
                    + "이미 좋아요한 상태에서 다시 요청해도 200으로 같은 결과를 응답합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "좋아요 완료",
                    content = @Content(schema = @Schema(implementation = LikeResponse.class))),
            @ApiResponse(responseCode = "401", description = "UNAUTHORIZED, TOKEN_EXPIRED, INVALID_TOKEN",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "PRIVATE_PLAYLIST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "PLAYLIST_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping
    public ResponseEntity<LikeResponse> like(
            @Parameter(description = "좋아요할 플레이리스트 ID", example = "1") @PathVariable Long playlistId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(likeService.like(Long.valueOf(userId), playlistId));
    }

    @Operation(
            summary = "좋아요 취소",
            description = "로그인 필요. 좋아요하지 않은 상태에서 요청해도 204로 응답합니다(멱등). "
                    + "비공개로 바뀌었거나 삭제된 플레이리스트도 취소할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "좋아요 취소 완료"),
            @ApiResponse(responseCode = "401", description = "UNAUTHORIZED, TOKEN_EXPIRED, INVALID_TOKEN",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping
    public ResponseEntity<Void> unlike(
            @Parameter(description = "좋아요를 취소할 플레이리스트 ID", example = "1") @PathVariable Long playlistId,
            @AuthenticationPrincipal String userId) {
        likeService.unlike(Long.valueOf(userId), playlistId);
        return ResponseEntity.noContent().build();
    }
}
