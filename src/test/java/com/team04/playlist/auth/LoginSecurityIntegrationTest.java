package com.team04.playlist.auth;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team04.playlist.global.exception.ErrorCode;
import com.team04.playlist.global.security.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@Import(LoginSecurityIntegrationTest.AuthCheckController.class)
class LoginSecurityIntegrationTest {

    private static final String PROTECTED_URI = "/api/v1/test/me";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @Value("${playlist.jwt.secret}")
    private String jwtSecret;

    @Nested
    @DisplayName("카카오 로그인")
    class KakaoLogin {

        @Test
        @DisplayName("로그인 시작 주소로 접근하면 카카오 로그인 페이지로 이동한다")
        void authorizationRedirect() throws Exception {
            mockMvc.perform(get("/oauth2/authorization/kakao"))
                    .andExpect(status().isFound())
                    .andExpect(header().string(HttpHeaders.LOCATION, allOf(
                            startsWith("https://kauth.kakao.com/oauth/authorize"),
                            containsString("client_id=test-client-id"),
                            containsString("redirect_uri="),
                            containsString("state="))));
        }

        @Test
        @DisplayName("로그인 시작 없이 들어온 콜백은 실패 핸들러가 401 OAUTH_LOGIN_FAILED로 응답한다")
        void callbackWithoutAuthorizationRequest() throws Exception {
            ResultActions result = mockMvc.perform(get("/login/oauth2/code/kakao")
                    .param("code", "fake-code")
                    .param("state", "fake-state"));

            expectError(result, ErrorCode.OAUTH_LOGIN_FAILED);
        }
    }

    @Nested
    @DisplayName("JWT 인증")
    class JwtAuthentication {

        @Test
        @DisplayName("유효한 access 토큰이면 인증되고 principal은 userId다")
        void validToken() throws Exception {
            String token = jwtProvider.createAccess("7");

            mockMvc.perform(get(PROTECTED_URI).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("7"));
        }

        @Test
        @DisplayName("토큰 없이 보호된 API를 호출하면 401 UNAUTHORIZED")
        void noToken() throws Exception {
            expectError(mockMvc.perform(get(PROTECTED_URI)), ErrorCode.UNAUTHORIZED);
        }

        @Test
        @DisplayName("만료된 토큰이면 401 TOKEN_EXPIRED")
        void expiredToken() throws Exception {
            String token = new JwtProvider(jwtSecret, -10, -10).createAccess("7");

            expectError(
                    mockMvc.perform(get(PROTECTED_URI).header(HttpHeaders.AUTHORIZATION, bearer(token))),
                    ErrorCode.TOKEN_EXPIRED);
        }

        @Test
        @DisplayName("다른 키로 서명된 토큰이면 401 INVALID_TOKEN")
        void forgedToken() throws Exception {
            String token = new JwtProvider("another-secret-key-that-is-long-enough-for-hs256", 1800, 604800)
                    .createAccess("7");

            expectError(
                    mockMvc.perform(get(PROTECTED_URI).header(HttpHeaders.AUTHORIZATION, bearer(token))),
                    ErrorCode.INVALID_TOKEN);
        }

        @Test
        @DisplayName("refresh 토큰으로 API를 호출하면 401 INVALID_TOKEN")
        void refreshTokenAsAccess() throws Exception {
            String token = jwtProvider.createRefresh("7");

            expectError(
                    mockMvc.perform(get(PROTECTED_URI).header(HttpHeaders.AUTHORIZATION, bearer(token))),
                    ErrorCode.INVALID_TOKEN);
        }
    }

    private static void expectError(ResultActions result, ErrorCode errorCode) throws Exception {
        result.andExpect(status().is(errorCode.getStatus().value()))
                .andExpect(jsonPath("$.status").value(errorCode.getStatus().value()))
                .andExpect(jsonPath("$.code").value(errorCode.name()))
                .andExpect(jsonPath("$.message").value(errorCode.getMessage()))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    // 인증 통과 여부만 확인하기 위한 테스트 전용 컨트롤러 (로그인 필요 경로)
    @RestController
    static class AuthCheckController {

        @GetMapping(PROTECTED_URI)
        String me(Authentication authentication) {
            return authentication.getName();
        }
    }
}
