package com.team04.playlist.playlist.controller;

import com.team04.playlist.global.response.PageResponse;
import com.team04.playlist.playlist.dto.PlaylistCreateRequest;
import com.team04.playlist.playlist.dto.PlaylistResponse;
import com.team04.playlist.playlist.dto.PlaylistSort;
import com.team04.playlist.playlist.dto.PlaylistSummaryResponse;
import com.team04.playlist.playlist.dto.PlaylistUpdateRequest;
import com.team04.playlist.playlist.service.PlaylistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    @PostMapping
    public ResponseEntity<PlaylistResponse> create(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody PlaylistCreateRequest request) {
        PlaylistResponse response = playlistService.create(toUserId(userId), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<PlaylistSummaryResponse>> getPublicPlaylists(
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<PlaylistSummaryResponse> response = playlistService.getPublicPlaylists(PlaylistSort.from(sort), page, size);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{playlistId}")
    public ResponseEntity<PlaylistResponse> update(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody PlaylistUpdateRequest request) {
        PlaylistResponse response = playlistService.update(playlistId, toUserId(userId), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{playlistId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal String userId) {
        playlistService.delete(playlistId, toUserId(userId));
        return ResponseEntity.noContent().build();
    }

    // JWT 필터가 넣어 둔 사용자 id(문자열)를 Long으로. 인증 정보가 없으면 null (서비스에서 401)
    private static Long toUserId(String userId) {
        return userId == null ? null : Long.valueOf(userId);
    }
}