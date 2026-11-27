package com.team04.playlist.playlist.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.team04.playlist.user.entity.Provider;
import com.team04.playlist.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PlaylistTest {

    private static final Long OWNER_ID = 1L;

    private final User owner = newUser(OWNER_ID);

    @Test
    @DisplayName("생성: 입력값 저장, 좋아요 수 0, 삭제 상태 아님")
    void create() {
        Playlist playlist = Playlist.create(owner, "제목", "설명", false);

        assertThat(playlist.getOwner()).isSameAs(owner);
        assertThat(playlist.getTitle()).isEqualTo("제목");
        assertThat(playlist.isPublic()).isFalse();
        assertThat(playlist.getLikeCount()).isZero();
        assertThat(playlist.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("수정: 제목, 설명, 공개 여부 변경")
    void update() {
        Playlist playlist = Playlist.create(owner, "제목", null, false);

        playlist.update("수정된 제목", "수정된 설명", true);

        assertThat(playlist.getTitle()).isEqualTo("수정된 제목");
        assertThat(playlist.getDescription()).isEqualTo("수정된 설명");
        assertThat(playlist.isPublic()).isTrue();
    }

    @Test
    @DisplayName("소프트 삭제: deletedAt 설정, 재호출 시 값 유지")
    void softDelete() {
        Playlist playlist = Playlist.create(owner, "제목", null, true);

        playlist.softDelete();
        var firstDeletedAt = playlist.getDeletedAt();
        playlist.softDelete();

        assertThat(playlist.isDeleted()).isTrue();
        assertThat(playlist.getDeletedAt()).isEqualTo(firstDeletedAt);
    }

    @Test
    @DisplayName("소유자 확인: 소유자 true, 다른 사용자 false, null false")
    void isOwnedBy() {
        Playlist playlist = Playlist.create(owner, "제목", null, true);

        assertThat(playlist.isOwnedBy(OWNER_ID)).isTrue();
        assertThat(playlist.isOwnedBy(2L)).isFalse();
        assertThat(playlist.isOwnedBy(null)).isFalse();
    }

    // id는 DB가 채우는 값이라 테스트에서는 직접 넣는다
    private static User newUser(Long id) {
        User user = User.builder().provider(Provider.KAKAO).providerId(String.valueOf(id)).nickname("user" + id).build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
