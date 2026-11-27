package com.team04.playlist.global.config;


import com.team04.playlist.global.exception.BusinessException;
import com.team04.playlist.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Set;

@Configuration
public class CookieOriginConfig implements WebMvcConfigurer {
    // 쿠키 POST를 허용할 Origin 목록입니다. 사용자 권한 목록과는 다릅니다.
    private final Set<String> allowedOrigins;

    public CookieOriginConfig(@Value("${playlist.cookie.allowed-origins}") Set<String> allowedOrigins) {
        this.allowedOrigins = Set.copyOf(allowedOrigins);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
                .addInterceptor(
                        new HandlerInterceptor() {
                            @Override
                            public boolean preHandle(
                                    HttpServletRequest request, HttpServletResponse response, Object handler) {
                                if (!"POST".equalsIgnoreCase(request.getMethod())) {
                                    return true;
                                }
                                String origin = request.getHeader("Origin");
                                if (origin == null || !allowedOrigins.contains(origin)) {
                                    throw new BusinessException(ErrorCode.INVALID_ORIGIN);
                                }
                                return true;
                            }
                        })
                .addPathPatterns(
                        "/api/v1/auth/reissue",
                        "/api/v1/auth/logout");
    }
}
