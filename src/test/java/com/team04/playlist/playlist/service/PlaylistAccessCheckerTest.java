package com.team04.playlist.playlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.team04.playlist.collaboration.service.CollaboratorReaderService;
import com.team04.playlist.global.exception.BusinessException;
import com.team04.playlist.global.exception.ErrorCode;
import com.team04.playlist.playlist.entity.Playlist;
import com.team04.playlist.playlist.entity.PlaylistRole;
import com.team04.playlist.playlist.repository.PlaylistRepository;
import java.util.Optional;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;


class PlaylistAccessCheckerTest {

    private static final Long PLAYLIST_ID = 10L;
    private static final Long OWNER_ID = 1L;
    private static final Long COLLABORATOR_ID = 2L;
    private static final Long VIEWER_ID = 3L;

    private final PlaylistRepository playlistRepository = mock(PlaylistRepository.class);
    private final CollaboratorReaderService collaboratorReaderService = mock(CollaboratorReaderService.class);
    private final PlaylistAccessChecker checker = new PlaylistAccessChecker(playlistRepository, collaboratorReaderService);

    // ===== 조회 (getViewable) =====

    @Test
    @DisplayName("조회: 공개 + 비로그인 → 통과")
    void viewPublicWithoutLogin() {
        Playlist playlist = givenPlaylist(true);

        assertThat(checker.getViewable(PLAYLIST_ID, null)).isSameAs(playlist);
    }

    @Test
    @DisplayName("조회: 비공개 + 비로그인 → 401 UNAUTHORIZED")
    void viewPrivateWithoutLogin() {
        givenPlaylist(false);

        assertError(() -> checker.getViewable(PLAYLIST_ID, null), ErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("조회: 비공개 + VIEWER → 403 PRIVATE_PLAYLIST")
    void viewPrivateByViewer() {
        givenPlaylist(false);

        assertError(() -> checker.getViewable(PLAYLIST_ID, VIEWER_ID), ErrorCode.PRIVATE_PLAYLIST);
    }

    @Test
    @DisplayName("조회: 비공개 + OWNER·COLLABORATOR → 통과")
    void viewPrivateByOwnerAndCollaborator() {
        Playlist playlist = givenPlaylist(false);
        givenCollaborator(COLLABORATOR_ID);

        assertThat(checker.getViewable(PLAYLIST_ID, OWNER_ID)).isSameAs(playlist);
        assertThat(checker.getViewable(PLAYLIST_ID, COLLABORATOR_ID)).isSameAs(playlist);
    }

    @Test
    @DisplayName("조회: 없음·삭제됨 → 404 PLAYLIST_NOT_FOUND")
    void viewNotFound() {
        given(playlistRepository.findByIdAndDeletedAtIsNull(PLAYLIST_ID)).willReturn(Optional.empty());

        assertError(() -> checker.getViewable(PLAYLIST_ID, OWNER_ID), ErrorCode.PLAYLIST_NOT_FOUND);
    }

    // ===== 트랙 편집 (getEditable) =====

    @Test
    @DisplayName("편집: 비로그인 → 401 UNAUTHORIZED (조회 전)")
    void editWithoutLogin() {
        assertError(() -> checker.getEditable(PLAYLIST_ID, null), ErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("편집: 공개 + VIEWER → 403 NOT_PLAYLIST_EDITOR")
    void editPublicByViewer() {
        givenPlaylist(true);

        assertError(() -> checker.getEditable(PLAYLIST_ID, VIEWER_ID), ErrorCode.NOT_PLAYLIST_EDITOR);
    }

    @Test
    @DisplayName("편집: 비공개 + VIEWER → 403 PRIVATE_PLAYLIST (비공개 검사 우선)")
    void editPrivateByViewer() {
        givenPlaylist(false);

        assertError(() -> checker.getEditable(PLAYLIST_ID, VIEWER_ID), ErrorCode.PRIVATE_PLAYLIST);
    }

    @Test
    @DisplayName("편집: 비공개 + OWNER·COLLABORATOR → 통과")
    void editByOwnerAndCollaborator() {
        Playlist playlist = givenPlaylist(false);
        givenCollaborator(COLLABORATOR_ID);

        assertThat(checker.getEditable(PLAYLIST_ID, OWNER_ID)).isSameAs(playlist);
        assertThat(checker.getEditable(PLAYLIST_ID, COLLABORATOR_ID)).isSameAs(playlist);
    }

    // ===== OWNER 전용 (getOwned) =====

    @Test
    @DisplayName("OWNER 전용: COLLABORATOR → 403 NOT_PLAYLIST_OWNER")
    void ownedByCollaborator() {
        givenPlaylist(false);
        givenCollaborator(COLLABORATOR_ID);

        assertError(() -> checker.getOwned(PLAYLIST_ID, COLLABORATOR_ID), ErrorCode.NOT_PLAYLIST_OWNER);
    }

    @Test
    @DisplayName("OWNER 전용: 공개 + VIEWER → 403 NOT_PLAYLIST_OWNER")
    void ownedByViewer() {
        givenPlaylist(true);

        assertError(() -> checker.getOwned(PLAYLIST_ID, VIEWER_ID), ErrorCode.NOT_PLAYLIST_OWNER);
    }

    @Test
    @DisplayName("OWNER 전용: OWNER → 통과")
    void ownedByOwner() {
        Playlist playlist = givenPlaylist(false);

        assertThat(checker.getOwned(PLAYLIST_ID, OWNER_ID)).isSameAs(playlist);
    }

    // ===== 역할 계산 (resolveRole) =====

    @Test
    @DisplayName("역할 계산: 소유자 OWNER, 편집자 COLLABORATOR, 그 외·비로그인 VIEWER")
    void resolveRole() {
        Playlist playlist = newPlaylist(true);
        givenCollaborator(COLLABORATOR_ID);

        assertThat(checker.resolveRole(playlist, OWNER_ID)).isEqualTo(PlaylistRole.OWNER);
        assertThat(checker.resolveRole(playlist, COLLABORATOR_ID)).isEqualTo(PlaylistRole.COLLABORATOR);
        assertThat(checker.resolveRole(playlist, VIEWER_ID)).isEqualTo(PlaylistRole.VIEWER);
        assertThat(checker.resolveRole(playlist, null)).isEqualTo(PlaylistRole.VIEWER);
    }

    // ===== 도우미 =====

    // OWNER_ID가 만든 플레이리스트. id는 DB가 채우는 값이라 테스트에서는 직접 넣는다
    private Playlist newPlaylist(boolean isPublic) {
        Playlist playlist = Playlist.create(OWNER_ID, "제목", null, isPublic);
        ReflectionTestUtils.setField(playlist, "id", PLAYLIST_ID);
        return playlist;
    }

    // Repository가 이 플레이리스트를 찾아 주도록 설정한다
    private Playlist givenPlaylist(boolean isPublic) {
        Playlist playlist = newPlaylist(isPublic);
        given(playlistRepository.findByIdAndDeletedAtIsNull(PLAYLIST_ID)).willReturn(Optional.of(playlist));
        return playlist;
    }

    private void givenCollaborator(Long userId) {
        given(collaboratorReaderService.isCollaborator(PLAYLIST_ID, userId)).willReturn(true);
    }

    private void assertError(ThrowingCallable call, ErrorCode expected) {
        assertThatThrownBy(call)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(expected);
    }
}