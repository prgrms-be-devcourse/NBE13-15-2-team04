package com.team04.playlist.like.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.team04.playlist.global.exception.BusinessException;
import com.team04.playlist.global.exception.ErrorCode;
import com.team04.playlist.like.dto.LikeResponse;
import com.team04.playlist.like.repository.PlaylistLikeRepository;
import com.team04.playlist.playlist.entity.Playlist;
import com.team04.playlist.playlist.service.PlaylistAccessChecker;
import com.team04.playlist.playlist.service.PlaylistLikeCounter;
import com.team04.playlist.user.entity.Provider;
import com.team04.playlist.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    private static final Long USER_ID = 7L;
    private static final Long PLAYLIST_ID = 10L;

    @Mock
    private PlaylistLikeRepository playlistLikeRepository;

    @Mock
    private PlaylistAccessChecker playlistAccessChecker;

    @Mock
    private PlaylistLikeCounter playlistLikeCounter;

    @InjectMocks
    private LikeService likeService;

    @Nested
    @DisplayName("좋아요 등록")
    class Like {

        @Test
        @DisplayName("처음 누르면 like_count +1, 증가 후 값을 응답한다")
        void firstLike() {
            given(playlistAccessChecker.getViewable(PLAYLIST_ID, USER_ID)).willReturn(playlist(42));
            given(playlistLikeRepository.insertIgnore(USER_ID, PLAYLIST_ID)).willReturn(1);
            given(playlistLikeCounter.increaseLikeCount(PLAYLIST_ID)).willReturn(43);

            LikeResponse response = likeService.like(USER_ID, PLAYLIST_ID);

            assertThat(response).isEqualTo(new LikeResponse(PLAYLIST_ID, true, 43));
        }

        @Test
        @DisplayName("이미 누른 상태면 증가 없이 현재 좋아요 수로 응답한다 (멱등)")
        void alreadyLiked() {
            given(playlistAccessChecker.getViewable(PLAYLIST_ID, USER_ID)).willReturn(playlist(42));
            given(playlistLikeRepository.insertIgnore(USER_ID, PLAYLIST_ID)).willReturn(0);

            LikeResponse response = likeService.like(USER_ID, PLAYLIST_ID);

            assertThat(response).isEqualTo(new LikeResponse(PLAYLIST_ID, true, 42));
            verify(playlistLikeCounter, never()).increaseLikeCount(anyLong());
        }

        @Test
        @DisplayName("비로그인 → 401 UNAUTHORIZED, 플레이리스트 조회·저장 안 함")
        void withoutLogin() {
            assertThatThrownBy(() -> likeService.like(null, PLAYLIST_ID))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.UNAUTHORIZED);

            verify(playlistAccessChecker, never()).getViewable(anyLong(), anyLong());
            verify(playlistLikeRepository, never()).insertIgnore(anyLong(), anyLong());
        }

        @Test
        @DisplayName("조회할 수 없는 플레이리스트(비공개) → 403 PRIVATE_PLAYLIST, 저장 안 함")
        void privatePlaylist() {
            given(playlistAccessChecker.getViewable(PLAYLIST_ID, USER_ID))
                    .willThrow(new BusinessException(ErrorCode.PRIVATE_PLAYLIST));

            assertThatThrownBy(() -> likeService.like(USER_ID, PLAYLIST_ID))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.PRIVATE_PLAYLIST);

            verify(playlistLikeRepository, never()).insertIgnore(anyLong(), anyLong());
        }

        @Test
        @DisplayName("없거나 삭제된 플레이리스트 → 404 PLAYLIST_NOT_FOUND, 저장 안 함")
        void notFoundPlaylist() {
            given(playlistAccessChecker.getViewable(PLAYLIST_ID, USER_ID))
                    .willThrow(new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND));

            assertThatThrownBy(() -> likeService.like(USER_ID, PLAYLIST_ID))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.PLAYLIST_NOT_FOUND);

            verify(playlistLikeRepository, never()).insertIgnore(anyLong(), anyLong());
        }
    }

    @Nested
    @DisplayName("좋아요 취소")
    class Unlike {

        @Test
        @DisplayName("좋아요가 있었으면 삭제 후 like_count -1")
        void unlike() {
            given(playlistLikeRepository.deleteByUserIdAndPlaylistId(USER_ID, PLAYLIST_ID)).willReturn(1);

            likeService.unlike(USER_ID, PLAYLIST_ID);

            verify(playlistLikeCounter).decreaseLikeCount(PLAYLIST_ID);
        }

        @Test
        @DisplayName("좋아요가 없었으면 감소하지 않는다 (멱등)")
        void notLiked() {
            given(playlistLikeRepository.deleteByUserIdAndPlaylistId(USER_ID, PLAYLIST_ID)).willReturn(0);

            likeService.unlike(USER_ID, PLAYLIST_ID);

            verify(playlistLikeCounter, never()).decreaseLikeCount(anyLong());
        }

        @Test
        @DisplayName("플레이리스트 공개 여부는 검사하지 않는다 (비공개·삭제돼도 취소 가능)")
        void noViewableCheck() {
            given(playlistLikeRepository.deleteByUserIdAndPlaylistId(USER_ID, PLAYLIST_ID)).willReturn(1);

            likeService.unlike(USER_ID, PLAYLIST_ID);

            verify(playlistAccessChecker, never()).getViewable(anyLong(), anyLong());
        }

        @Test
        @DisplayName("비로그인 → 401 UNAUTHORIZED, 삭제 안 함")
        void withoutLogin() {
            assertThatThrownBy(() -> likeService.unlike(null, PLAYLIST_ID))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.UNAUTHORIZED);

            verify(playlistLikeRepository, never()).deleteByUserIdAndPlaylistId(anyLong(), anyLong());
        }
    }

    @Nested
    @DisplayName("좋아요 여부")
    class IsLiked {

        @Test
        @DisplayName("비로그인이면 조회 없이 false")
        void withoutLogin() {
            assertThat(likeService.isLiked(null, PLAYLIST_ID)).isFalse();

            verify(playlistLikeRepository, never()).existsByUserIdAndPlaylistId(anyLong(), anyLong());
        }

        @Test
        @DisplayName("로그인이면 좋아요 행 존재 여부를 반환")
        void withLogin() {
            given(playlistLikeRepository.existsByUserIdAndPlaylistId(USER_ID, PLAYLIST_ID)).willReturn(true);

            assertThat(likeService.isLiked(USER_ID, PLAYLIST_ID)).isTrue();
        }
    }

    //getViewable 이 돌려줄 플레이리스트. likeCount 는 DB가 관리하는 값이라 테스트에서는 직접 넣는다
    private static Playlist playlist(int likeCount) {
        User owner = User.builder().provider(Provider.KAKAO).providerId("1").nickname("owner").build();
        Playlist playlist = Playlist.create(owner, "제목", null, true);
        ReflectionTestUtils.setField(playlist, "id", PLAYLIST_ID);
        ReflectionTestUtils.setField(playlist, "likeCount", likeCount);
        return playlist;
    }
}
