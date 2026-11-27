package com.team04.playlist.playlist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlaylistCreateRequest(

        @NotBlank(message = "제목을 입력해 주세요.")
        @Size(max = 30, message = "제목은 30자 이하여야 합니다.")
        String title,

        @Size(max = 200, message = "설명은 200자 이하여야 합니다.")
        String description,

        Boolean isPublic
) {

    // isPublic을 보내지 않았으면 공개
    public boolean isPublicOrDefault() {
        return isPublic == null || isPublic;
    }
}