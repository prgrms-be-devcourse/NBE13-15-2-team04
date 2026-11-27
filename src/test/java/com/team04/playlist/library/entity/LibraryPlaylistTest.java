package com.team04.playlist.library.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LibraryPlaylistTest {

    @Test
    @DisplayName("of()로 생성하면 libraryId와 playlistId가 설정되고 id, createdAt은 null이다")
    void of_setsLibraryIdAndPlaylistId() {
        LibraryPlaylist libraryPlaylist = LibraryPlaylist.of(10L, 100L);

        assertThat(libraryPlaylist.getLibraryId()).isEqualTo(10L);
        assertThat(libraryPlaylist.getPlaylistId()).isEqualTo(100L);
        assertThat(libraryPlaylist.getId()).isNull();
        assertThat(libraryPlaylist.getCreatedAt()).isNull();
    }
}