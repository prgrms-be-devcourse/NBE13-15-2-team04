package com.team04.playlist.library.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LibraryTest {

    @Test
    @DisplayName("기본 보관함 생성 시 userId, name이 설정되고 isDefault는 true다")
    void createDefault_setsFieldsAndIsDefaultTrue() {
        Library library = Library.createDefault(1L);
        assertThat(library.getUserId()).isEqualTo(1L);
        assertThat(library.getName()).isEqualTo("기본 보관함");
        assertThat(library.isDefault()).isTrue();
        assertThat(library.getId()).isNull();
        assertThat(library.getCreatedAt()).isNull();
    }
}