package com.pge.mockservices.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Generates a signed HS256 JWT bearer token for outbound Kong-secured HTTP calls.
 *
 * Token payload:  { "iss": "<issuer>", "exp": <unix-epoch-seconds> }
 * Authorization header value:  Bearer <token>
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken() {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .issuer(issuer)
                .expiration(new Date(expiration * 1000L))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String bearerToken() {
        return "Bearer " + generateToken();
    }
}
