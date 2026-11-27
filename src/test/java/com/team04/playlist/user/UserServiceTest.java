package com.team04.playlist.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.team04.playlist.user.entity.Provider;
import com.team04.playlist.user.entity.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String KAKAO_ID = "12345";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("이미 가입한 사용자면 저장하지 않고 기존 사용자를 반환한다")
    void existingUser() {
        User existing = user("홍길동");
        given(userRepository.findByProviderAndProviderId(Provider.KAKAO, KAKAO_ID))
                .willReturn(Optional.of(existing));

        User result = userService.findOrCreate(Provider.KAKAO, KAKAO_ID, "다른닉네임");

        assertThat(result).isSameAs(existing);
        verify(userRepository, never()).save(any());
    }

    @Nested
    @DisplayName("신규 가입 시 닉네임 생성")
    class NewUserNickname {

        @BeforeEach
        void setUp() {
            given(userRepository.findByProviderAndProviderId(Provider.KAKAO, KAKAO_ID))
                    .willReturn(Optional.empty());
            given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));
        }

        @Test
        @DisplayName("사용 중이 아닌 닉네임은 앞뒤 공백만 제거하고 그대로 쓴다")
        void unique() {
            given(userRepository.existsByNickname("홍길동")).willReturn(false);

            User result = userService.findOrCreate(Provider.KAKAO, KAKAO_ID, "  홍길동 ");

            assertThat(result.getNickname()).isEqualTo("홍길동");
            assertThat(result.getProvider()).isEqualTo(Provider.KAKAO);
            assertThat(result.getProviderId()).isEqualTo(KAKAO_ID);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("닉네임이 없거나 비어 있으면 기본 닉네임을 쓴다")
        void blank(String nickname) {
            given(userRepository.existsByNickname(User.DEFAULT_NICKNAME)).willReturn(false);

            User result = userService.findOrCreate(Provider.KAKAO, KAKAO_ID, nickname);

            assertThat(result.getNickname()).isEqualTo(User.DEFAULT_NICKNAME);
        }

        @Test
        @DisplayName("최대 길이를 넘으면 글자 수 기준으로 자르고, 이모지가 깨지지 않는다")
        void truncate() {
            String nickname = "가".repeat(19) + "😀😀";
            given(userRepository.existsByNickname(anyString())).willReturn(false);

            User result = userService.findOrCreate(Provider.KAKAO, KAKAO_ID, nickname);

            String saved = result.getNickname();
            assertThat(saved.codePointCount(0, saved.length())).isEqualTo(User.NICKNAME_MAX_LENGTH);
            assertThat(saved).isEqualTo("가".repeat(19) + "😀");
        }

        @Test
        @DisplayName("이미 사용 중인 닉네임이면 뒤에 4자리 숫자를 붙인다")
        void duplicate() {
            given(userRepository.existsByNickname("홍길동")).willReturn(true);
            given(userRepository.existsByNickname(org.mockito.ArgumentMatchers.matches("홍길동\\d{4}")))
                    .willReturn(false);

            User result = userService.findOrCreate(Provider.KAKAO, KAKAO_ID, "홍길동");

            assertThat(result.getNickname()).matches("홍길동[1-9]\\d{3}");
        }

        @Test
        @DisplayName("긴 닉네임이 중복이면 숫자를 붙여도 최대 길이를 넘지 않는다")
        void longDuplicate() {
            String truncated = "a".repeat(User.NICKNAME_MAX_LENGTH);
            given(userRepository.existsByNickname(truncated)).willReturn(true);
            given(userRepository.existsByNickname(org.mockito.ArgumentMatchers.matches("a{16}\\d{4}")))
                    .willReturn(false);

            User result = userService.findOrCreate(Provider.KAKAO, KAKAO_ID, "a".repeat(25));

            assertThat(result.getNickname())
                    .hasSize(User.NICKNAME_MAX_LENGTH)
                    .matches("a{16}[1-9]\\d{3}");
        }

        @Test
        @DisplayName("숫자를 붙여도 계속 중복이면 기본 닉네임 뒤에 랜덤 문자를 붙인다")
        void allRetriesCollide() {
            given(userRepository.existsByNickname(anyString())).willReturn(true);

            User result = userService.findOrCreate(Provider.KAKAO, KAKAO_ID, "홍길동");

            assertThat(result.getNickname())
                    .startsWith(User.DEFAULT_NICKNAME)
                    .hasSize(User.NICKNAME_MAX_LENGTH)
                    .matches(User.DEFAULT_NICKNAME + "[0-9a-f]+");
        }
    }

    private static User user(String nickname) {
        return User.builder()
                .provider(Provider.KAKAO)
                .providerId(KAKAO_ID)
                .nickname(nickname)
                .build();
    }
}
