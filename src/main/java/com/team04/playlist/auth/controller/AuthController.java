package com.team04.playlist.auth.controller;

import com.team04.playlist.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "토큰 재발급", description = "리프레시 토큰으로 액세스/리프레시 토큰을 재발급합니다.")
    @SecurityRequirements // 로그인 없이 호출 가능
    @PostMapping("/reissue")
    @ResponseStatus(HttpStatus.OK)
    public void reissue() {
        throw new UnsupportedOperationException("토큰 재발급 구현 예정");
    }

    @Operation(summary = "로그아웃", description = "저장된 리프레시 토큰을 폐기합니다.")
    @SecurityRequirements // 로그인 없이 호출 가능 (리프레시 쿠키로 처리)
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
        throw new UnsupportedOperationException("로그아웃 구현 예정");
    }
}
