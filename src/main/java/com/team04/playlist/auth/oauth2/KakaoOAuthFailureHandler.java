package com.team04.playlist.auth.oauth2;

import com.team04.playlist.global.exception.ErrorCode;
import com.team04.playlist.global.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOAuthFailureHandler implements AuthenticationFailureHandler {

    private final JsonMapper jsonMapper;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {
        // 실패 원인은 응답에 노출하지 않고 로그로만 남긴다.
        log.warn("OAuth2 login failed: {}", exception.getMessage());

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        ErrorCode errorCode = ErrorCode.OAUTH_LOGIN_FAILED;
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(jsonMapper.writeValueAsString(ErrorResponse.of(errorCode)));
    }
}
