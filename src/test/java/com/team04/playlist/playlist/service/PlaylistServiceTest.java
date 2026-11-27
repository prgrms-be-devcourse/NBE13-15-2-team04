package com.team04.playlist.playlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.team04.playlist.global.exception.BusinessException;
import com.team04.playlist.global.exception.ErrorCode;
import com.team04.playlist.playlist.dto.PlaylistCreateRequest;
import com.team04.playlist.playlist.dto.PlaylistResponse;
import com.team04.playlist.playlist.dto.PlaylistSort;
import com.team04.playlist.playlist.dto.PlaylistUpdateRequest;
import com.team04.playlist.playlist.entity.Playlist;
import com.team04.playlist.playlist.repository.PlaylistRepository;
import com.team04.playlist.user.UserService;
import com.team04.playlist.user.entity.Provider;
import com.team04.playlist.user.entity.User;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;


class PlaylistServiceTest {

    private static final Long PLAYLIST_ID = 10L;
    private static final Long OWNER_ID = 1L;

    private final PlaylistRepository playlistRepository = mock(PlaylistRepository.class);
    private final PlaylistAccessChecker accessChecker = mock(PlaylistAccessChecker.class);
    private final UserService userService = mock(UserService.class);
    private final EntityManager entityManager = mock(EntityManager.class);
    private final PlaylistService playlistService = new PlaylistService(playlistRepository, accessChecker, userService, entityManager);

    private final User owner = newUser(OWNER_ID);

    @Test
    @DisplayName("생성: 소유자 = 요청자, isPublic 생략 시 true")
    void create() {
        given(entityManager.getReference(User.class, OWNER_ID)).willReturn(owner);
        given(playlistRepository.save(any(Playlist.class))).willAnswer(invocation -> invocation.getArgument(0));

        PlaylistResponse response = playlistService.create(OWNER_ID, new PlaylistCreateRequest("제목", null, null));

        assertThat(response.ownerId()).isEqualTo(OWNER_ID);
        assertThat(response.ownerNickname()).isEqualTo("user1");
        assertThat(response.title()).isEqualTo("제목");
        assertThat(response.isPublic()).isTrue();
        assertThat(response.likeCount()).isZero();
    }

    @Test
    @DisplayName("생성: 비로그인 → 401 UNAUTHORIZED, 저장 안 함")
    void createWithoutLogin() {
        assertThatThrownBy(() -> playlistService.create(null, new PlaylistCreateRequest("제목", null, true)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
        verify(playlistRepository, never()).save(any());
    }

    @Test
    @DisplayName("생성: 없는 사용자 → 404 USER_NOT_FOUND, 저장 안 함")
    void createWithUnknownUser() {
        given(userService.getUserSummary(99L)).willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

        assertThatThrownBy(() -> playlistService.create(99L, new PlaylistCreateRequest("제목", null, true)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
        verify(playlistRepository, never()).save(any());
    }

    @Test
    @DisplayName("수정: 보낸 값만 변경, null 값 유지")
    void updateOnlySentFields() {
        Playlist playlist = givenOwnedPlaylist("제목", "설명", false);

        PlaylistResponse response = playlistService.update(PLAYLIST_ID, OWNER_ID, new PlaylistUpdateRequest(null, null, true));

        assertThat(response.title()).isEqualTo("제목");
        assertThat(response.description()).isEqualTo("설명");
        assertThat(response.isPublic()).isTrue();
        assertThat(playlist.isPublic()).isTrue();
    }

    @Test
    @DisplayName("수정: OWNER 아님 → 403 NOT_PLAYLIST_OWNER")
    void updateByNotOwner() {
        given(accessChecker.getOwned(PLAYLIST_ID, 2L)).willThrow(new BusinessException(ErrorCode.NOT_PLAYLIST_OWNER));

        assertThatThrownBy(() -> playlistService.update(PLAYLIST_ID, 2L, new PlaylistUpdateRequest("수정된 제목", null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_PLAYLIST_OWNER);
    }

    @Test
    @DisplayName("삭제: 소프트 삭제, repository.delete 호출 안 함")
    void delete() {
        Playlist playlist = givenOwnedPlaylist("제목", null, true);

        playlistService.delete(PLAYLIST_ID, OWNER_ID);

        assertThat(playlist.isDeleted()).isTrue();
        verify(playlistRepository, never()).delete(any());
    }

    @Test
    @DisplayName("공개 목록: 좋아요순 정렬 전달, size 100 → 50")
    void getPublicPlaylists() {
        Playlist playlist = Playlist.create(owner, "제목", null, true);
        ReflectionTestUtils.setField(playlist, "id", PLAYLIST_ID);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        given(playlistRepository.findByIsPublicTrueAndDeletedAtIsNull(pageable.capture()))
                .willAnswer(invocation -> new PageImpl<>(List.of(playlist), invocation.getArgument(0), 1));

        var response = playlistService.getPublicPlaylists(PlaylistSort.LIKES, 0, 100);

        assertThat(pageable.getValue().getPageSize()).isEqualTo(50);
        assertThat(pageable.getValue().getSort()).isEqualTo(PlaylistSort.LIKES.toSort());
        assertThat(response.content()).extracting("playlistId").containsExactly(PLAYLIST_ID);
        assertThat(response.size()).isEqualTo(50);
        assertThat(response.hasNext()).isFalse();
    }

    // OWNER_ID가 만든 10번 플레이리스트를 검사기가 통과시켜 주도록 설정한다
    private Playlist givenOwnedPlaylist(String title, String description, boolean isPublic) {
        Playlist playlist = Playlist.create(owner, title, description, isPublic);
        ReflectionTestUtils.setField(playlist, "id", PLAYLIST_ID);
        given(accessChecker.getOwned(PLAYLIST_ID, OWNER_ID)).willReturn(playlist);
        return playlist;
    }

    // id는 DB가 채우는 값이라 테스트에서는 직접 넣는다
    private static User newUser(Long id) {
        User user = User.builder().provider(Provider.KAKAO).providerId(String.valueOf(id)).nickname("user" + id).build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}