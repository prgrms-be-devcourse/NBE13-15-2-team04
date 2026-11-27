package com.team04.playlist.auth.oauth2;

import static org.assertj.core.api.Assertions.assertThat;

import com.team04.playlist.global.security.JwtProvider;
import com.team04.playlist.user.UserRepository;
import com.team04.playlist.user.entity.Provider;
import com.team04.playlist.user.entity.User;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * 카카오 로그인 성공 이후 흐름을 실제 빈과 DB(H2, schema.sql)로 검증한다.
 * 카카오 서버와의 통신(토큰 교환, 사용자 정보 조회)만 생략하고,
 * 그 결과인 인증 객체를 직접 만들어 성공 핸들러를 호출한다.
 * 테스트마다 트랜잭션을 롤백해 서로 영향을 주지 않는다.
 */
@SpringBootTest
@Transactional
class KakaoLoginIntegrationTest {

    @Autowired
    private KakaoOAuthSuccessHandler successHandler;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    @DisplayName("처음 로그인하면 회원으로 저장되고, 저장된 회원 id로 토큰 2개가 발급된다")
    void firstLogin() throws Exception {
        assertThat(userRepository.count()).isZero();

        MockHttpServletResponse tokens = login("1001", "홍길동");

        User saved = userRepository.findByProviderAndProviderId(Provider.KAKAO, "1001").orElseThrow();
        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(saved.getNickname()).isEqualTo("홍길동");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertTokensBelongTo(tokens, saved.getId());
    }

    @Test
    @DisplayName("같은 카카오 계정으로 다시 로그인하면 새로 저장하지 않고 같은 회원으로 토큰이 발급된다")
    void secondLoginSameAccount() throws Exception {
        login("1001", "홍길동");
        Long userId = userRepository.findByProviderAndProviderId(Provider.KAKAO, "1001").orElseThrow().getId();

        // 카카오에서 닉네임을 바꿨더라도 우리 서비스의 닉네임은 유지된다.
        MockHttpServletResponse tokens = login("1001", "카카오에서바꾼이름");

        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(userRepository.findById(userId).orElseThrow().getNickname()).isEqualTo("홍길동");
        assertTokensBelongTo(tokens, userId);
    }

    @Test
    @DisplayName("다른 카카오 계정이 같은 닉네임으로 가입하면 숫자가 붙은 닉네임으로 저장된다")
    void duplicateNicknameFromAnotherAccount() throws Exception {
        login("1001", "홍길동");

        login("2002", "홍길동");

        User second = userRepository.findByProviderAndProviderId(Provider.KAKAO, "2002").orElseThrow();
        assertThat(userRepository.count()).isEqualTo(2);
        assertThat(second.getNickname()).matches("홍길동[1-9]\\d{3}");
    }

    @Test
    @DisplayName("카카오 닉네임이 없으면 기본 닉네임으로 가입된다")
    void noNickname() throws Exception {
        login("1001", null);

        User saved = userRepository.findByProviderAndProviderId(Provider.KAKAO, "1001").orElseThrow();
        assertThat(saved.getNickname()).isEqualTo(User.DEFAULT_NICKNAME);
    }

    private MockHttpServletResponse login(String kakaoId, String nickname) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        successHandler.onAuthenticationSuccess(
                new MockHttpServletRequest(), response, kakaoAuthentication(kakaoId, nickname));
        assertThat(response.getStatus()).isEqualTo(200);
        return response;
    }

    // access 토큰은 바디에서, refresh 토큰은 쿠키에서 꺼내 확인한다.
    private void assertTokensBelongTo(MockHttpServletResponse response, Long userId) throws Exception {
        String expected = String.valueOf(userId);
        JsonNode body = jsonMapper.readTree(response.getContentAsString());
        assertThat(jwtProvider.parseAccess(body.get("accessToken").asString()).getSubject()).isEqualTo(expected);
        assertThat(jwtProvider.parseRefresh(response.getCookie("refreshToken").getValue()).getSubject()).isEqualTo(expected);
    }

    // 카카오 사용자 정보 응답(/v2/user/me)을 받은 뒤 Spring Security가 만드는 인증 객체와 같은 형태
    private static OAuth2AuthenticationToken kakaoAuthentication(String kakaoId, String nickname) {
        Map<String, Object> profile = nickname == null ? Map.of() : Map.of("nickname", nickname);
        Map<String, Object> attributes = Map.of(
                "id", Long.parseLong(kakaoId),
                "kakao_account", Map.of("profile", profile));
        DefaultOAuth2User oauth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")), attributes, "id");
        return new OAuth2AuthenticationToken(oauth2User, oauth2User.getAuthorities(), "kakao");
    }
}
