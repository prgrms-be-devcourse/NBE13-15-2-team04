package com.team04.playlist.like.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.team04.playlist.playlist.entity.Playlist;
import com.team04.playlist.user.entity.Provider;
import com.team04.playlist.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PlaylistLikeRepositoryTest {

    @Autowired
    private PlaylistLikeRepository playlistLikeRepository;

    @Autowired
    private TestEntityManager em;

    private Long userId;
    private Long playlistId;

    @BeforeEach
    void setUp() {
        User user = em.persist(User.builder().provider(Provider.KAKAO).providerId("1").nickname("liker").build());
        Playlist playlist = em.persist(Playlist.create(user, "제목", null, true));
        em.flush();
        userId = user.getId();
        playlistId = playlist.getId();
    }

    @Test
    @DisplayName("insertIgnore: 처음이면 1, 같은 조합을 다시 넣으면 예외 없이 0")
    void insertIgnore() {
        int first = playlistLikeRepository.insertIgnore(userId, playlistId);
        int second = playlistLikeRepository.insertIgnore(userId, playlistId);

        assertThat(first).isEqualTo(1);
        assertThat(second).isZero();
        assertThat(playlistLikeRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("delete: 있으면 1, 다시 지우면 0")
    void delete() {
        playlistLikeRepository.insertIgnore(userId, playlistId);

        int first = playlistLikeRepository.deleteByUserIdAndPlaylistId(userId, playlistId);
        int second = playlistLikeRepository.deleteByUserIdAndPlaylistId(userId, playlistId);

        assertThat(first).isEqualTo(1);
        assertThat(second).isZero();
        assertThat(playlistLikeRepository.count()).isZero();
    }

    @Test
    @DisplayName("exists: 좋아요 행이 있을 때만 true")
    void exists() {
        assertThat(playlistLikeRepository.existsByUserIdAndPlaylistId(userId, playlistId)).isFalse();

        playlistLikeRepository.insertIgnore(userId, playlistId);

        assertThat(playlistLikeRepository.existsByUserIdAndPlaylistId(userId, playlistId)).isTrue();
    }
}
