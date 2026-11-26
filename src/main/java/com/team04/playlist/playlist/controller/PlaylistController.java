package com.team04.playlist.playlist.controller;

import com.team04.playlist.playlist.dto.PlaylistCreateRequest;
import com.team04.playlist.playlist.dto.PlaylistPageResponse;
import com.team04.playlist.playlist.dto.PlaylistResponse;
import com.team04.playlist.playlist.dto.PlaylistSort;
import com.team04.playlist.playlist.dto.PlaylistSuccessResponse;
import com.team04.playlist.playlist.dto.PlaylistUpdateRequest;
import com.team04.playlist.playlist.service.PlaylistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    //TODO: 로그인(JWT)이 들어오기 전까지 이 헤더로 사용자 id를 받음
    private static final String USER_ID_HEADER = "X-User-Id";

    private final PlaylistService playlistService;

    @PostMapping
    public ResponseEntity<PlaylistSuccessResponse<PlaylistResponse>> create(
            @RequestHeader(value = USER_ID_HEADER, required = false) Long userId,
            @Valid @RequestBody PlaylistCreateRequest request) {
        PlaylistResponse response = playlistService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PlaylistSuccessResponse.created("플레이리스트를 생성했습니다.", response));
    }

    @GetMapping
    public PlaylistSuccessResponse<PlaylistPageResponse> getPublicPlaylists(
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PlaylistPageResponse response = playlistService.getPublicPlaylists(PlaylistSort.from(sort), page, size);
        return PlaylistSuccessResponse.ok("플레이리스트 목록을 조회했습니다.", response);
    }

    @PatchMapping("/{playlistId}")
    public PlaylistSuccessResponse<PlaylistResponse> update(
            @PathVariable Long playlistId,
            @RequestHeader(value = USER_ID_HEADER, required = false) Long userId,
            @Valid @RequestBody PlaylistUpdateRequest request) {
        PlaylistResponse response = playlistService.update(playlistId, userId, request);
        return PlaylistSuccessResponse.ok("플레이리스트를 수정했습니다.", response);
    }

    @DeleteMapping("/{playlistId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long playlistId,
            @RequestHeader(value = USER_ID_HEADER, required = false) Long userId) {
        playlistService.delete(playlistId, userId);
        return ResponseEntity.noContent().build();
    }
}