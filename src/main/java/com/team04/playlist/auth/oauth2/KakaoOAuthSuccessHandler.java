package com.team04.playlist.auth.oauth2;


import com.team04.playlist.auth.cookie.CookieAuthWriter;
import com.team04.playlist.auth.dto.TokenResponse;
import com.team04.playlist.global.exception.BusinessException;
import com.team04.playlist.global.exception.ErrorCode;
import com.team04.playlist.global.exception.ErrorResponse;
import com.team04.playlist.global.security.JwtProvider;
import com.team04.playlist.user.UserService;
import com.team04.playlist.user.entity.Provider;
import com.team04.playlist.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final JsonMapper jsonMapper;
    private final CookieAuthWriter cookieAuthWriter;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        // OAuth2 로그인 과정에서 state 저장용으로 만든 임시 세션은 더 이상 필요 없다.
        invalidateSession(request);

        OAuth2AuthenticationToken oauth = (OAuth2AuthenticationToken) authentication;
        String kakaoId = oauth.getName();
        if (kakaoId == null || kakaoId.isBlank()) {
            writeError(response, ErrorCode.OAUTH_LOGIN_FAILED);
            return;
        }

        User user;
        try {
            user = this.userService.findOrCreate(Provider.KAKAO, kakaoId, nickname(oauth));
        } catch (BusinessException ex) {
            log.warn("OAuth2 user link failed: {}", ex.getErrorCode());
            writeError(response, ex.getErrorCode());
            return;
        } catch (RuntimeException ex) {
            log.error("OAuth2 user link failed", ex);
            writeError(response, ErrorCode.INTERNAL_SERVER_ERROR);
            return;
        }

        String userId = String.valueOf(user.getId());
        String accessToken = this.jwtProvider.createAccess(userId);
        String refreshToken = this.jwtProvider.createRefresh(userId);
        // TODO: 재발급/로그아웃 검증을 위해 refresh 토큰을 Redis에 저장

        // 토큰이 담긴 응답은 캐시되지 않도록 한다.
        response.setHeader("Cache-Control", "no-store");
        // TODO: 지금은 콜백 응답(JSON)이 브라우저 화면에 그대로 표시되어 프론트(SPA)가 액세스 토큰을 읽을 수 없다.
        //       reissue 구현 시 프론트로 302 리다이렉트하는 방식으로 바꾸고, 토큰 전달 방법을 함께 결정한다.
        // 리프레시 토큰은 httpOnly 쿠키로, 액세스 토큰만 바디로 내려준다.
        // 쿠키 헤더는 바디를 쓰기 전에 추가해야 응답에 포함된다.
        this.cookieAuthWriter.writeRefresh(response, refreshToken);
        writeJson(response, HttpServletResponse.SC_OK, new TokenResponse(accessToken));
    }

    private void writeError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        writeJson(response, errorCode.getStatus().value(), ErrorResponse.of(errorCode));
    }

    private void writeJson(HttpServletResponse response, int status, Object body) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(jsonMapper.writeValueAsString(body));
    }

    private static void invalidateSession(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    // 카카오 사용자 정보(kakao_account.profile.nickname)에서 닉네임을 읽는다.
    // 없거나 비어 있으면 null을 반환하고, 기본 닉네임은 UserService가 정한다.
    private static String nickname(OAuth2AuthenticationToken oauth) {
        if (!(oauth.getPrincipal().getAttributes().get("kakao_account") instanceof Map<?, ?> account)) {
            return null;
        }
        if (!(account.get("profile") instanceof Map<?, ?> profile)) {
            return null;
        }
        Object name = profile.get("nickname");
        return (name == null || name.toString().isBlank()) ? null : name.toString();
    }
}
