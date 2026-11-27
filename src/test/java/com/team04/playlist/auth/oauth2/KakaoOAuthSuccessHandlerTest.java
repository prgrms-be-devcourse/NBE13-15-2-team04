package com.team04.playlist.auth.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.team04.playlist.auth.cookie.CookieAuthWriter;
import com.team04.playlist.global.exception.BusinessException;
import com.team04.playlist.global.exception.ErrorCode;
import com.team04.playlist.global.security.JwtProvider;
import com.team04.playlist.user.UserService;
import com.team04.playlist.user.entity.Provider;
import com.team04.playlist.user.entity.User;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class KakaoOAuthSuccessHandlerTest {

    private static final String KAKAO_ID = "12345";
    private static final String SECRET = "test-only-jwt-secret-key-for-unit-and-integration-tests";

    private final UserService userService = mock(UserService.class);
    private final JwtProvider jwtProvider = new JwtProvider(SECRET, 1800, 604800);
    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final KakaoOAuthSuccessHandler handler =
            new KakaoOAuthSuccessHandler(userService, jwtProvider, jsonMapper, new CookieAuthWriter(false, 604800));

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @Test
    @DisplayName("로그인에 성공하면 access 토큰은 바디로, refresh 토큰은 httpOnly 쿠키로 응답하고 임시 세션을 정리한다")
    void success() throws Exception {
        given(userService.findOrCreate(Provider.KAKAO, KAKAO_ID, "홍길동")).willReturn(userWithId(7L));
        MockHttpSession session = (MockHttpSession) request.getSession(true);

        handler.onAuthenticationSuccess(request, response, kakaoAuthentication("홍길동"));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
        assertThat(session.isInvalid()).isTrue();

        JsonNode body = body();
        assertThat(jwtProvider.parseAccess(body.get("accessToken").asString()).getSubject()).isEqualTo("7");
        assertThat(body.has("refreshToken")).isFalse();

        Cookie refresh = response.getCookie("refreshToken");
        assertThat(refresh).isNotNull();
        assertThat(refresh.isHttpOnly()).isTrue();
        assertThat(refresh.getPath()).isEqualTo("/api/v1/auth");
        assertThat(refresh.getMaxAge()).isEqualTo(604800);
        assertThat(jwtProvider.parseRefresh(refresh.getValue()).getSubject()).isEqualTo("7");
    }

    @Test
    @DisplayName("카카오 닉네임이 없으면 null을 넘겨 기본 닉네임은 UserService가 정한다")
    void noNickname() throws Exception {
        given(userService.findOrCreate(any(), any(), isNull())).willReturn(userWithId(7L));

        handler.onAuthenticationSuccess(request, response, kakaoAuthentication(null));

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("회원 처리 중 BusinessException이 나면 해당 에러 코드로 응답한다")
    void businessException() throws Exception {
        given(userService.findOrCreate(any(), any(), any()))
                .willThrow(new BusinessException(ErrorCode.DUPLICATE_NICKNAME));

        handler.onAuthenticationSuccess(request, response, kakaoAuthentication("홍길동"));

        assertErrorResponse(ErrorCode.DUPLICATE_NICKNAME);
    }

    @Test
    @DisplayName("예상하지 못한 예외는 내부 메시지를 숨기고 500으로 응답한다")
    void unexpectedException() throws Exception {
        given(userService.findOrCreate(any(), any(), any()))
                .willThrow(new IllegalStateException("db connection lost"));

        handler.onAuthenticationSuccess(request, response, kakaoAuthentication("홍길동"));

        assertErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR);
        assertThat(response.getContentAsString()).doesNotContain("db connection lost");
    }

    private void assertErrorResponse(ErrorCode errorCode) throws Exception {
        assertThat(response.getStatus()).isEqualTo(errorCode.getStatus().value());
        JsonNode body = body();
        assertThat(body.get("status").asInt()).isEqualTo(errorCode.getStatus().value());
        assertThat(body.get("code").asString()).isEqualTo(errorCode.name());
        assertThat(body.get("message").asString()).isEqualTo(errorCode.getMessage());
        assertThat(body.has("timestamp")).isTrue();
    }

    private JsonNode body() throws Exception {
        return jsonMapper.readTree(response.getContentAsString());
    }

    private static OAuth2AuthenticationToken kakaoAuthentication(String nickname) {
        Map<String, Object> profile = nickname == null ? Map.of() : Map.of("nickname", nickname);
        Map<String, Object> attributes = Map.of(
                "id", Long.parseLong(KAKAO_ID),
                "kakao_account", Map.of("profile", profile));
        DefaultOAuth2User oauth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")), attributes, "id");
        return new OAuth2AuthenticationToken(oauth2User, oauth2User.getAuthorities(), "kakao");
    }

    private static User userWithId(Long id) {
        User user = User.builder()
                .provider(Provider.KAKAO)
                .providerId(KAKAO_ID)
                .nickname("홍길동")
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
