// src/main/java/com/admineasy/security/JwtUtils.java
package com.admineasy.security;

import com.admineasy.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtils {

    /**
     * Chaîne Base64 de 512 bits (64 octets) pour HS512.
     * Par exemple générée avec : openssl rand -base64 64
     * En production, stockez idéalement dans une variable d’environnement.
     */
    @Value("${jwt.secret}")
    private String jwtSecretBase64;

    /**
     * Durée de vie du token : 24 heures
     */
    private final long jwtExpirationMs = 24 * 60 * 60 * 1000L;

    /**
     * Décode la chaîne Base64 et retourne le tableau d’octets
     */
    private byte[] getSecretKeyBytes() {
        return Base64.getDecoder().decode(jwtSecretBase64);
    }

    /**
     * Génère un token JWT signé en HS512 contenant l’id, l’email et l’orgId de l’utilisateur.
     */
    public String generateJwtToken(User user) {
        byte[] keyBytes = getSecretKeyBytes();
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("orgId", user.getOrganization().getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, keyBytes)
                .compact();
    }

    /**
     * Récupère l’identifiant utilisateur (subject) depuis le token.
     */
    public Long getUserIdFromJwt(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSecretKeyBytes())
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }

    /**
     * Récupère l’orgId depuis le token (custom claim “orgId”).
     */
    public Long getOrgIdFromJwt(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSecretKeyBytes())
                .parseClaimsJws(token)
                .getBody();
        return claims.get("orgId", Long.class);
    }

    /**
     * Valide la signature et l’expiration du token.
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .setSigningKey(getSecretKeyBytes())
                    .parseClaimsJws(authToken);
            return true;
        } catch (JwtException e) {
            // Signature invalide, token expiré, etc.
            return false;
        }
    }
}
