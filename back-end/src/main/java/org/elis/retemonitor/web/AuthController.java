package org.elis.retemonitor.web;

import java.security.Principal;
import java.util.Map;

import org.elis.retemonitor.servizio.ServizioAutenticazione;
import org.elis.retemonitor.sicurezza.ServizioJwt.TokenEmesso;
import org.elis.retemonitor.web.dto.CredenzialiRichiesta;
import org.elis.retemonitor.web.dto.RispostaAccesso;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ServizioAutenticazione servizio;

    public AuthController(ServizioAutenticazione servizio) {
        this.servizio = servizio;
    }

    /** Pubblico: dice al frontend se mostrare "Crea l'account" oppure "Accedi". */
    @GetMapping("/stato")
    public Map<String, Boolean> stato() {
        return Map.of("configurato", servizio.configurato());
    }

    /** Pubblico, ma solo finché non esiste nessun utente. */
    @PostMapping("/configura")
    @ResponseStatus(HttpStatus.CREATED)
    public RispostaAccesso configura(@Valid @RequestBody CredenzialiRichiesta credenziali) {
        TokenEmesso token = servizio.configura(credenziali.username(), credenziali.password());
        return new RispostaAccesso(token.token(), credenziali.username().trim(), token.scadenza());
    }

    @PostMapping("/login")
    public RispostaAccesso login(@Valid @RequestBody CredenzialiRichiesta credenziali) {
        TokenEmesso token = servizio.accedi(credenziali.username(), credenziali.password());
        return new RispostaAccesso(token.token(), credenziali.username().trim(), token.scadenza());
    }

    /** Protetto: restituisce chi è collegato (serve al frontend per verificare il token). */
    @GetMapping("/io")
    public Map<String, String> io(Principal utente) {
        return Map.of("username", utente.getName());
    }
}
