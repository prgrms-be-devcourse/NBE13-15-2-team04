package com.team04.playlist.auth.dto;

// 로그인 성공, 토큰 재발급 시 응답
// 리프레시 토큰은 바디에 담지 않고 httpOnly 쿠키(refreshToken)로 내려준다. (CookieAuthWriter)
public record TokenResponse(
        String accessToken
) {
}
