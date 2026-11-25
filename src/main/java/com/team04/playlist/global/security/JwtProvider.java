package com.team04.playlist.global.security;

import com.team04.playlist.global.exception.BusinessException;
import com.team04.playlist.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtProvider {
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";
    public static final String CLAIM_TYPE = "tokenType";

    private final SecretKey key;
    private final long accessSeconds;
    private final long refreshSeconds;

    public JwtProvider(
            @Value("${playlist.jwt.secret}") String secret,
            @Value("${playlist.jwt.access-seconds}") long accessSeconds,
            @Value("${playlist.jwt.refresh-seconds}") long refreshSeconds) {

        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessSeconds = accessSeconds;
        this.refreshSeconds = refreshSeconds;
    }

    public String createAccess(String loginId) {
        return compact(loginId, TYPE_ACCESS, this.accessSeconds);
    }

    public String createRefresh(String loginId) {
        return compact(loginId, TYPE_REFRESH, this.refreshSeconds);
    }

    public Claims parseAccess(String token) {
        return parseWithType(token, TYPE_ACCESS);
    }

    public Claims parseRefresh(String token) {
        return parseWithType(token, TYPE_REFRESH);
    }

    public String bearerValue(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer "))
            throw new BusinessException(ErrorCode.INVALID_TOKEN);

        String token = authorization.substring("Bearer ".length()).trim();

        if (token.isBlank())
            throw new BusinessException(ErrorCode.INVALID_TOKEN);

        return token;
    }

    private String compact(String userId, String tokenType, long seconds) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId) //user 의 id
                .id(java.util.UUID.randomUUID().toString())
                .claim(CLAIM_TYPE, tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(seconds)))
                .signWith(this.key, Jwts.SIG.HS256)
                .compact();
    }

    private Claims parseWithType(String token, String expectedType) {
        if (token == null || token.isBlank())
            throw new BusinessException(ErrorCode.INVALID_TOKEN);

        try {
            Claims claims =
                    Jwts.parser().verifyWith(this.key).build().parseSignedClaims(token).getPayload();
            Object type = claims.get(CLAIM_TYPE);
            if (type == null || !expectedType.equals(type.toString())) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN);
            }
            if (claims.getSubject() == null
                    || claims.getSubject().isBlank()
                    || claims.getExpiration() == null) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN);
            }
            return claims;
        } catch (ExpiredJwtException ex) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }
}
