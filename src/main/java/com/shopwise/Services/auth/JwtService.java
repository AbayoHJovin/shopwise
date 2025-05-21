package com.shopwise.Services.auth;

import com.shopwise.models.Employee;
import com.shopwise.models.User;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtService {
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;
    private final String secret = "92c9ecbb7ce30f51b15b64943ebac510e7c59cdadd326965b401f30213433377ab3c38a467d65befb544939d0e1917891abea061a90";
    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId().toString());
        claims.put("role", user.getRole().name());
        claims.put("type", "user");

        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSignKey() , Jwts.SIG.HS256)
                .compact();
    }

    public String generateToken(Employee employee) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", employee.getId().toString());
        claims.put("role", employee.getRole().name());
        claims.put("type", "employee");

        return Jwts.builder()
                .claims(claims)
                .subject(employee.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSignKey() , Jwts.SIG.HS256)
                .compact();
    }


    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUserId(String token) {
        return extractClaims(token).get("id", String.class);
    }
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }
    public String extractType(String token) {
        return extractClaims(token).get("type", String.class);
    }

}
