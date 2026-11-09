package org.elis.retemonitor.sicurezza;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/** Crea e verifica i token JWT firmati con HMAC-SHA. Non dipende da Spring, così si prova facilmente. */
public class ServizioJwt {

    private final SecretKey chiave;
    private final Duration durata;

    /**
     * @param segreto chiave di almeno 32 caratteri; se vuota ne viene generata una casuale
     * @param durata  validità di ogni token
     */
    public ServizioJwt(String segreto, Duration durata) {
        this.chiave = Keys.hmacShaKeyFor(byteChiave(segreto));
        this.durata = durata;
    }

    public TokenEmesso crea(String username) {
        Instant adesso = Instant.now();
        Instant scadenza = adesso.plus(durata);
        String token = Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(adesso))
                .expiration(Date.from(scadenza))
                .signWith(chiave)
                .compact();
        return new TokenEmesso(token, scadenza);
    }

    /** Restituisce l'username contenuto nel token, oppure vuoto se il token è falso, alterato o scaduto. */
    public Optional<String> verifica(String token) {
        try {
            String username = Jwts.parser()
                    .verifyWith(chiave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return Optional.ofNullable(username);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static byte[] byteChiave(String segreto) {
        if (segreto == null || segreto.isBlank()) {
            byte[] casuali = new byte[64];
            new SecureRandom().nextBytes(casuali);
            return casuali;
        }
        byte[] chiave = segreto.getBytes(StandardCharsets.UTF_8);
        if (chiave.length < 32) {
            throw new IllegalStateException("jwt.segreto (JWT_SEGRETO) deve avere almeno 32 caratteri");
        }
        return chiave;
    }

    public record TokenEmesso(String token, Instant scadenza) {
    }
}
