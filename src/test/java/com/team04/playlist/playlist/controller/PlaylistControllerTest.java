package com.team04.playlist.playlist.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team04.playlist.global.exception.BusinessException;
import com.team04.playlist.global.exception.ErrorCode;
import com.team04.playlist.global.response.PageResponse;
import com.team04.playlist.playlist.dto.PlaylistCreateRequest;
import com.team04.playlist.playlist.dto.PlaylistResponse;
import com.team04.playlist.playlist.dto.PlaylistSort;
import com.team04.playlist.playlist.dto.PlaylistSummaryResponse;
import com.team04.playlist.playlist.service.PlaylistService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;


@WebMvcTest(PlaylistController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlaylistControllerTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 11, 27, 10, 0);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaylistService playlistService;

    @AfterEach
    void logout() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("생성: 201, 플레이리스트 정보를 바로 응답")
    void create() throws Exception {
        given(playlistService.create(eq(1L), any(PlaylistCreateRequest.class)))
                .willReturn(new PlaylistResponse(10L, 1L, "user1", "제목", null, false, 0, CREATED_AT));

        mockMvc.perform(post("/api/v1/playlists")
                        .with(loginUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "제목", "isPublic": false}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.playlistId").value(10))
                .andExpect(jsonPath("$.ownerNickname").value("user1"))
                .andExpect(jsonPath("$.isPublic").value(false))
                .andExpect(jsonPath("$.createdAt").value("2026-11-27T10:00:00"));
    }

    @Test
    @DisplayName("생성: 빈 제목 → 400 INVALID_INPUT")
    void createWithBlankTitle() throws Exception {
        mockMvc.perform(post("/api/v1/playlists")
                        .with(loginUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "  "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("제목을 입력해 주세요."));
    }

    @Test
    @DisplayName("생성: 제목 31자 → 400 INVALID_INPUT")
    void createWithTooLongTitle() throws Exception {
        mockMvc.perform(post("/api/v1/playlists")
                        .with(loginUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + "가".repeat(31) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("수정: NOT_PLAYLIST_OWNER → 403 에러 응답")
    void updateByNotOwner() throws Exception {
        given(playlistService.update(eq(10L), eq(2L), any()))
                .willThrow(new BusinessException(ErrorCode.NOT_PLAYLIST_OWNER));

        mockMvc.perform(patch("/api/v1/playlists/10")
                        .with(loginUser(2L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "수정된 제목"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("NOT_PLAYLIST_OWNER"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("삭제: 204, 본문 없음")
    void deletePlaylist() throws Exception {
        mockMvc.perform(delete("/api/v1/playlists/10").with(loginUser(1L)))
                .andExpect(status().isNoContent());

        verify(playlistService).delete(10L, 1L);
    }

    @Test
    @DisplayName("삭제: 삭제된 플레이리스트 → 404 PLAYLIST_NOT_FOUND")
    void deleteNotFound() throws Exception {
        willThrow(new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND)).given(playlistService).delete(10L, 1L);

        mockMvc.perform(delete("/api/v1/playlists/10").with(loginUser(1L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PLAYLIST_NOT_FOUND"));
    }

    @Test
    @DisplayName("공개 목록: 비로그인 200, 페이지 정보 포함")
    void getPublicPlaylists() throws Exception {
        PageResponse<PlaylistSummaryResponse> page = new PageResponse<>(
                List.of(new PlaylistSummaryResponse(10L, 1L, "user1", "제목", 42, CREATED_AT)), 0, 20, 135, true);
        given(playlistService.getPublicPlaylists(PlaylistSort.LIKES, 0, 20)).willReturn(page);

        mockMvc.perform(get("/api/v1/playlists").param("sort", "likes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("제목"))
                .andExpect(jsonPath("$.content[0].ownerNickname").value("user1"))
                .andExpect(jsonPath("$.content[0].likeCount").value(42))
                .andExpect(jsonPath("$.totalElements").value(135))
                .andExpect(jsonPath("$.hasNext").value(true));
    }

    @Test
    @DisplayName("공개 목록: 잘못된 sort → 400 INVALID_INPUT")
    void getPublicPlaylistsWithWrongSort() throws Exception {
        mockMvc.perform(get("/api/v1/playlists").param("sort", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    // 필터를 끈 테스트라 JWT 필터 대신 사용자 id(문자열)를 로그인 정보로 직접 넣는다
    private static RequestPostProcessor loginUser(Long userId) {
        return request -> {
            SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                    String.valueOf(userId), null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
            return request;
        };
    }
}