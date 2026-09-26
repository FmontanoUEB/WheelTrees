package co.edu.unbosque.wheeltrees.security;

import co.edu.unbosque.wheeltrees.config.JwtProperties;
import co.edu.unbosque.wheeltrees.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    public String generarToken(Usuario usuario) {
        return Jwts.builder()
                .subject(usuario.getId().toString())
                .claim("email", usuario.getEmail())
                .claim("rol", usuario.getRol().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpirationMs()))
                .signWith(obtenerClave())
                .compact();
    }

    public boolean validarToken(String token) {
        try {
            obtenerClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public UUID extraerUsuarioId(String token) {
        return UUID.fromString(obtenerClaims(token).getSubject());
    }

    public String extraerEmail(String token) {
        return obtenerClaims(token).get("email", String.class);
    }

    private Claims obtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(obtenerClave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey obtenerClave() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}