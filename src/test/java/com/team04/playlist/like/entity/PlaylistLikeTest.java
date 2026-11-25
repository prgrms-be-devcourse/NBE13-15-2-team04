package com.team04.playlist.like.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class PlaylistLikeTest {

    @Test
    @DisplayName("of()로 생성하면 userId와 playlistId가 설정된다")
    void of_setsUserIdAndPlaylistId() {
        PlaylistLike like = PlaylistLike.of(1L, 10L);

        assertThat(ReflectionTestUtils.getField(like, "userId")).isEqualTo(1L);
        assertThat(ReflectionTestUtils.getField(like, "playlistId")).isEqualTo(10L);
    }

    @Test
    @DisplayName("저장 전에는 id와 createdAt이 비어 있다")
    void of_idAndCreatedAtAreNullBeforePersist() {
        PlaylistLike like = PlaylistLike.of(1L, 10L);

        assertThat(ReflectionTestUtils.getField(like, "id")).isNull();
        assertThat(ReflectionTestUtils.getField(like, "createdAt")).isNull();
    }
}
