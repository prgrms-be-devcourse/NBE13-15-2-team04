package com.team04.playlist.auth.cookie;


import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieAuthWriter {

    private static final String REFRESH = "refreshToken";
    private static final String REFRESH_PATH = "/api/v1/auth";

    private final boolean secure;
    private final Duration refreshMaxAge;

    // 쿠키 만료시간은 리프레시 토큰 만료시간(playlist.jwt.refresh-seconds)과 같은 값을 씁니다.
    public CookieAuthWriter(
            @Value("${playlist.cookie.secure}") boolean secure,
            @Value("${playlist.jwt.refresh-seconds}") long refreshSeconds) {
        this.secure = secure;
        this.refreshMaxAge = Duration.ofSeconds(refreshSeconds);
    }

    public void writeRefresh(HttpServletResponse response, String refreshToken) {
        addRefreshCookie(response, refreshToken, refreshMaxAge);
    }

    // 삭제할 때도 path·sameSite 등 속성이 발급 때와 같아야 브라우저가 같은 쿠키로 보고 지웁니다.
    public void clearRefresh(HttpServletResponse response) {
        addRefreshCookie(response, "", Duration.ZERO);
    }

    private void addRefreshCookie(HttpServletResponse response, String value, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH, value)
                .httpOnly(true)
                .secure(secure)
                .path(REFRESH_PATH)
                .sameSite("Lax")
                .maxAge(maxAge)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
