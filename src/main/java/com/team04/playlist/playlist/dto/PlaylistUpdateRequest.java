package com.team04.playlist.playlist.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PlaylistUpdateRequest(

        @Pattern(regexp = ".*\\S.*", message = "제목을 입력해 주세요.")
        @Size(max = 30, message = "제목은 30자 이하여야 합니다.")
        String title,

        @Size(max = 200, message = "설명은 200자 이하여야 합니다.")
        String description,

        Boolean isPublic
) {
}