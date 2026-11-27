package com.team04.playlist.auth.oauth2;

import static org.assertj.core.api.Assertions.assertThat;

import com.team04.playlist.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class KakaoOAuthFailureHandlerTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final KakaoOAuthFailureHandler handler = new KakaoOAuthFailureHandler(jsonMapper);

    @Test
    @DisplayName("소셜 로그인에 실패하면 401 OAUTH_LOGIN_FAILED로 응답하고 실패 원인은 노출하지 않는다")
    void failure() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession session = (MockHttpSession) request.getSession(true);
        var exception = new OAuth2AuthenticationException(
                new OAuth2Error("invalid_token_response"), "secret internal detail");

        handler.onAuthenticationFailure(request, response, exception);

        ErrorCode errorCode = ErrorCode.OAUTH_LOGIN_FAILED;
        assertThat(response.getStatus()).isEqualTo(errorCode.getStatus().value());
        assertThat(session.isInvalid()).isTrue();

        JsonNode body = jsonMapper.readTree(response.getContentAsString());
        assertThat(body.get("code").asString()).isEqualTo(errorCode.name());
        assertThat(body.get("message").asString()).isEqualTo(errorCode.getMessage());
        assertThat(response.getContentAsString()).doesNotContain("secret internal detail");
    }
}
