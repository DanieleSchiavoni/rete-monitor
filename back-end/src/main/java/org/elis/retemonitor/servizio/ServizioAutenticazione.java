package org.elis.retemonitor.servizio;

import java.time.LocalDateTime;

import org.elis.retemonitor.modello.Utente;
import org.elis.retemonitor.repository.UtenteRepository;
import org.elis.retemonitor.sicurezza.ServizioJwt;
import org.elis.retemonitor.sicurezza.ServizioJwt.TokenEmesso;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestisce l'accesso alla dashboard. Al primo avvio non esistono utenti: la prima persona
 * che apre l'app crea l'account (configurazione iniziale), poi la registrazione si chiude.
 */
@Service
public class ServizioAutenticazione {

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;
    private final ServizioJwt servizioJwt;
    /** Hash usato quando l'username non esiste, così la risposta impiega lo stesso tempo. */
    private final String hashFittizio;

    public ServizioAutenticazione(UtenteRepository utenteRepository, PasswordEncoder passwordEncoder,
                                  ServizioJwt servizioJwt) {
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
        this.servizioJwt = servizioJwt;
        this.hashFittizio = passwordEncoder.encode("password-fittizia-per-tempi-costanti");
    }

    @Transactional(readOnly = true)
    public boolean configurato() {
        return utenteRepository.count() > 0;
    }

    /** Crea il primo account. Funziona una volta sola: dopo risponde con errore. */
    @Transactional
    public synchronized TokenEmesso configura(String username, String password) {
        if (configurato()) {
            throw new OperazioneNonConsentitaException("L'account è già stato creato: accedi con le tue credenziali");
        }
        utenteRepository.save(new Utente(username.trim(), passwordEncoder.encode(password), LocalDateTime.now()));
        return servizioJwt.crea(username.trim());
    }

    @Transactional(readOnly = true)
    public TokenEmesso accedi(String username, String password) {
        Utente utente = utenteRepository.findByUsername(username.trim()).orElse(null);
        String hash = utente != null ? utente.getPasswordHash() : hashFittizio;
        boolean passwordCorretta = passwordEncoder.matches(password, hash);
        if (utente == null || !passwordCorretta) {
            throw new CredenzialiNonValideException();
        }
        return servizioJwt.crea(utente.getUsername());
    }
}
