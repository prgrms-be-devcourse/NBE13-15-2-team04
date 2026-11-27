package com.team04.playlist.global.config;

import com.team04.playlist.global.security.JwtAuthFilter;
import com.team04.playlist.global.security.JwtAuthenticationEntryPoint;
import com.team04.playlist.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.NullRequestCache;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(cache -> cache.requestCache(new NullRequestCache()))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())
                .authorizeHttpRequests(auth -> auth
                        // 공통: 에러 페이지, Swagger, CORS preflight
                        .requestMatchers("/error", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 인증: 소셜 로그인 시작/콜백, 토큰 재발급, 로그아웃
                        // 재발급·로그아웃은 액세스 토큰 대신 리프레시 쿠키로 처리한다. (액세스 토큰이 만료돼도 로그아웃 가능)
                        .requestMatchers("/oauth2/authorization/**", "/login/oauth2/code/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/reissue", "/api/v1/auth/logout").permitAll()

                        // 플레이리스트 조회: 로그인 없이 접근 가능
                        // 비공개 플레이리스트 접근 제한(PRIVATE_PLAYLIST)은 서비스에서 판단한다.
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/playlists",
                                "/api/v1/playlists/ranking",
                                "/api/v1/playlists/{playlistId}",
                                "/api/v1/playlists/{playlistId}/collaborators").permitAll()

                        // 그 외는 로그인 필요. OWNER·COLLABORATOR 같은 역할 검사는 서비스에서 한다.
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        // 인증이 필요한데 인증이 없을 때 401 응답 (UNAUTHORIZED / TOKEN_EXPIRED / INVALID_TOKEN)
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .addFilterBefore(new JwtAuthFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
