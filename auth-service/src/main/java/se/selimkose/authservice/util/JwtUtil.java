package se.selimkose.authservice.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {
    private final Key secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey.getBytes(StandardCharsets.UTF_8));
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email, String role) {
        // Logic to generate JWT token using the secretKey, email, and role
        // This is a placeholder; actual implementation will depend on your JWT library
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // Token valid for 10 hour
                .signWith(secretKey)
                .compact();
    }

    public void validateToken(String token) {

        try {
            Jwts.parser().verifyWith((SecretKey) secretKey)
                    .build()
                    .parseClaimsJws(token);
        } catch (SignatureException e) {
            throw new JwtException("Invalid JWT Signature", e);
        } catch (JwtException e) {
            throw new JwtException("Invalid JWT", e);
        }
    }
}
