package com.team04.playlist.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.team04.playlist.global.exception.BusinessException;
import com.team04.playlist.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class JwtProviderTest {

    private static final String SECRET = "test-only-jwt-secret-key-for-unit-and-integration-tests";
    private static final String OTHER_SECRET = "another-secret-key-that-is-long-enough-for-hs256";

    private final JwtProvider jwtProvider = new JwtProvider(SECRET, 1800, 604800);

    @Nested
    @DisplayName("토큰 발급과 검증")
    class CreateAndParse {

        @Test
        @DisplayName("access 토큰을 발급하고 검증하면 subject로 userId를 꺼낼 수 있다")
        void access() {
            String token = jwtProvider.createAccess("1");

            Claims claims = jwtProvider.parseAccess(token);

            assertThat(claims.getSubject()).isEqualTo("1");
            assertThat(claims.get(JwtProvider.CLAIM_TYPE)).isEqualTo(JwtProvider.TYPE_ACCESS);
        }

        @Test
        @DisplayName("refresh 토큰을 발급하고 검증하면 subject로 userId를 꺼낼 수 있다")
        void refresh() {
            String token = jwtProvider.createRefresh("1");

            Claims claims = jwtProvider.parseRefresh(token);

            assertThat(claims.getSubject()).isEqualTo("1");
            assertThat(claims.get(JwtProvider.CLAIM_TYPE)).isEqualTo(JwtProvider.TYPE_REFRESH);
        }
    }

    @Nested
    @DisplayName("토큰 검증 실패")
    class ParseFailure {

        @Test
        @DisplayName("refresh 토큰을 access 토큰으로 검증하면 INVALID_TOKEN")
        void refreshAsAccess() {
            String refresh = jwtProvider.createRefresh("1");

            assertErrorCode(() -> jwtProvider.parseAccess(refresh), ErrorCode.INVALID_TOKEN);
        }

        @Test
        @DisplayName("access 토큰을 refresh 토큰으로 검증하면 INVALID_TOKEN")
        void accessAsRefresh() {
            String access = jwtProvider.createAccess("1");

            assertErrorCode(() -> jwtProvider.parseRefresh(access), ErrorCode.INVALID_TOKEN);
        }

        @Test
        @DisplayName("만료된 토큰은 TOKEN_EXPIRED")
        void expired() {
            JwtProvider expiredProvider = new JwtProvider(SECRET, -10, -10);
            String token = expiredProvider.createAccess("1");

            assertErrorCode(() -> jwtProvider.parseAccess(token), ErrorCode.TOKEN_EXPIRED);
        }

        @Test
        @DisplayName("다른 키로 서명된 토큰은 INVALID_TOKEN")
        void forged() {
            JwtProvider otherProvider = new JwtProvider(OTHER_SECRET, 1800, 604800);
            String token = otherProvider.createAccess("1");

            assertErrorCode(() -> jwtProvider.parseAccess(token), ErrorCode.INVALID_TOKEN);
        }

        @Test
        @DisplayName("다른 키로 서명된 만료 토큰은 TOKEN_EXPIRED가 아니라 INVALID_TOKEN")
        void forgedAndExpired() {
            JwtProvider otherExpiredProvider = new JwtProvider(OTHER_SECRET, -10, -10);
            String token = otherExpiredProvider.createAccess("1");

            assertErrorCode(() -> jwtProvider.parseAccess(token), ErrorCode.INVALID_TOKEN);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "not-a-jwt", "aaa.bbb.ccc"})
        @DisplayName("비어 있거나 형식이 깨진 토큰은 INVALID_TOKEN")
        void malformed(String token) {
            assertErrorCode(() -> jwtProvider.parseAccess(token), ErrorCode.INVALID_TOKEN);
        }
    }

    @Nested
    @DisplayName("Authorization 헤더에서 토큰 추출")
    class BearerValue {

        @Test
        @DisplayName("Bearer 접두사를 제거한 토큰을 반환한다")
        void success() {
            assertThat(jwtProvider.bearerValue("Bearer abc.def.ghi")).isEqualTo("abc.def.ghi");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"abc.def.ghi", "bearer abc.def.ghi", "Bearer ", "Bearer    "})
        @DisplayName("Bearer 접두사가 없거나 토큰이 비어 있으면 INVALID_TOKEN")
        void invalid(String header) {
            assertErrorCode(() -> jwtProvider.bearerValue(header), ErrorCode.INVALID_TOKEN);
        }
    }

    private static void assertErrorCode(
            org.assertj.core.api.ThrowableAssert.ThrowingCallable callable, ErrorCode errorCode) {
        assertThatThrownBy(callable)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }
}
