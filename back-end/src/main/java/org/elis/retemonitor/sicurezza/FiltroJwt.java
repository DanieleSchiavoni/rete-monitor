package org.elis.retemonitor.sicurezza;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Legge l'intestazione "Authorization: Bearer <token>" e, se il token è valido,
 * segna la richiesta come autenticata. Se manca o non è valido, Spring Security risponde 401.
 */
public class FiltroJwt extends OncePerRequestFilter {

    private static final String PREFISSO = "Bearer ";

    private final ServizioJwt servizioJwt;

    public FiltroJwt(ServizioJwt servizioJwt) {
        this.servizioJwt = servizioJwt;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest richiesta, HttpServletResponse risposta, FilterChain catena)
            throws ServletException, IOException {
        String intestazione = richiesta.getHeader("Authorization");
        if (intestazione != null && intestazione.startsWith(PREFISSO)) {
            servizioJwt.verifica(intestazione.substring(PREFISSO.length())).ifPresent(username -> {
                var autenticazione = new UsernamePasswordAuthenticationToken(
                        username, null, List.of(new SimpleGrantedAuthority("ROLE_UTENTE")));
                SecurityContextHolder.getContext().setAuthentication(autenticazione);
            });
        }
        catena.doFilter(richiesta, risposta);
    }
}
