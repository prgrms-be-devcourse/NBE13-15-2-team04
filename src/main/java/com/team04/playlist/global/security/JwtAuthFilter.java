package com.team04.playlist.global.security;

import com.team04.playlist.global.exception.BusinessException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if(header != null && header.startsWith("Bearer ")){
            try {
                String token = this.jwtProvider.bearerValue(header);

                Claims claims = this.jwtProvider.parseAccess(token);
                UsernamePasswordAuthenticationToken authentication =
                        UsernamePasswordAuthenticationToken
                                .authenticated(claims.getSubject(), null, List.of(
                                        new SimpleGrantedAuthority("ROLE_USER")));

                SecurityContext context = SecurityContextHolder.createEmptyContext();

                context.setAuthentication(authentication);

                SecurityContextHolder.setContext(context);

            }catch (BusinessException ex){
                SecurityContextHolder.clearContext();
                // 인증이 필요한 API라면 EntryPoint에서 이 코드(TOKEN_EXPIRED, INVALID_TOKEN)로 응답한다.
                request.setAttribute(JwtAuthenticationEntryPoint.ERROR_CODE_ATTRIBUTE, ex.getErrorCode());
            }
        }

        filterChain.doFilter(request, response);
    }
}