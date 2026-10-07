package com.example.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * El JWT representa la autenticación del usuario después del login.
 * Gestiona el ciclo de vida completo de los tokens JWT (JSON Web Tokens).
 *     1.- Genera los tokens        2.- Desencripta y extrae información.
 *     3.- Valida la integridad     4.- Verifica el tiempo del token
 */
@Service
public class JwtService {

//    En producción: mover a application.properties o usar una clave de 256 bits real
//    private static final String SECRET = "my-secret-key-my-secret-key-my-secret-key-12345678901234567890";

    @Value("${app.security.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // ===================== GENERAR TOKEN =====================
    // Nuevo método que acepta los claims (roles) y el username
    public String generateToken(Map<String, Object> extraClaims, String username) {
        return Jwts.builder()
                .claims(extraClaims) // <-- Inserta los roles en el payload de forma moderna
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1h
                .signWith(getSigningKey())
                .compact();
    }

    // Mantenemos el método original para evitar errores de retrocompatibilidad
    public String generateToken(String username) {
        return generateToken(new HashMap<>(), username);
    }

    // ===================== EXTRAER USERNAME =====================
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }


    // ===================== VALIDAR TOKEN =====================
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;               // Previene fallos si el token está corrupto o mal firmado
        }
    }

    // ===================== EXPIRACIÓN =====================
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }

    // ===================== PARSER CENTRAL MODERNIZADO =====================
    private Claims extractAllClaims(String token) {
        // En JJWT 0.12+ se usa parser(), verifyWith() y getPayload()
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ===================== EXTRAER ROLES =====================
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        List<String> roles = extractAllClaims(token).get("roles", List.class);
        return roles != null ? roles : Collections.emptyList();
    }
}
