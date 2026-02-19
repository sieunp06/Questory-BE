package com.ssafy.questory.config.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtService {

    @Value("${jwt.access.secret}")
    private String accessSecret;

    @Value("${jwt.access.expiration_time}")
    private long accessExpMs;

    @Value("${jwt.refresh.secret}")
    private String refreshSecret;

    @Value("${jwt.refresh.expiration_time}")
    private long refreshExpMs;

    private Key accessKey;
    private Key refreshKey;

    @PostConstruct
    void init() {
        this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
        this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));
    }

    public String generateAccessToken(UserDetails userDetails) {
        return buildToken(
                userDetails.getUsername(),
                Map.of("typ", TokenType.ACCESS.value),
                accessExpMs,
                accessKey
        );
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(
                userDetails.getUsername(),
                Map.of(
                        "typ", TokenType.REFRESH.value,
                        "jti", UUID.randomUUID().toString()
                ),
                refreshExpMs,
                refreshKey
        );
    }

    private String buildToken(String subject, Map<String, Object> claims, long expMs, Key signingKey) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expMs))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token, TokenType expectedType) {
        return extractClaim(token, expectedType, Claims::getSubject);
    }

    public <T> T extractClaim(String token, TokenType expectedType, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token, expectedType);
        return resolver.apply(claims);
    }

    public Claims extractAllClaims(String token, TokenType expectedType) {
        Jws<Claims> jws = parser(expectedType).parseClaimsJws(token);
        Claims claims = jws.getBody();

        String typ = claims.get("typ", String.class);
        if (typ == null || !typ.equals(expectedType.value)) {
            throw new JwtException("Invalid token type");
        }
        return claims;
    }

    private JwtParser parser(TokenType type) {
        return Jwts.parserBuilder()
                .setSigningKey(type == TokenType.ACCESS ? accessKey : refreshKey)
                .build();
    }

    public String extractJti(String token) {
        Claims claims = extractAllClaims(token, TokenType.REFRESH);
        return claims.get("jti", String.class);
    }

    public long getAccessExpMs() {
        return accessExpMs;
    }

    public long getRefreshExpMs() {
        return refreshExpMs;
    }

    public enum TokenType {
        ACCESS("access"),
        REFRESH("refresh");

        private final String value;
        TokenType(String value) { this.value = value; }
    }
}
