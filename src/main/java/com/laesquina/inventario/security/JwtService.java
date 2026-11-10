package com.laesquina.inventario.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Genera y valida los tokens JWT de la API.
 *
 * El token lleva el usuario (subject), su rol y la fecha de expiracion, y va firmado
 * con HS256 usando la clave app.jwt.secret. Si alguien modifica el token, la firma
 * deja de coincidir y la validacion falla.
 */
@Service
public class JwtService {

    private final SecretKey clave;
    private final Duration duracion;

    public JwtService(@Value("${app.jwt.secret}") String secreto,
                      @Value("${app.jwt.expiracion-minutos}") long minutos) {
        if (secreto == null || secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.jwt.secret (JWT_SECRET) debe tener al menos 32 caracteres");
        }
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.duracion = Duration.ofMinutes(minutos);
    }

    // Token con la duracion configurada.
    public String generarToken(String usuario, String rol) {
        return generarToken(usuario, rol, duracion);
    }

    // Token con una duracion concreta (las pruebas la usan para crear un token ya expirado).
    public String generarToken(String usuario, String rol, Duration validez) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuario)
                .claim("rol", rol)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(validez)))
                .signWith(clave, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Valida la firma y la expiracion, y devuelve los datos del token.
     * Lanza JwtException si el token esta alterado, mal formado o expirado.
     */
    public Claims validar(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getDuracionSegundos() {
        return duracion.toSeconds();
    }
}
