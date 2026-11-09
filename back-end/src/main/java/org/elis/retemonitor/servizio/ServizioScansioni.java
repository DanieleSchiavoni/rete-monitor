package org.elis.retemonitor.servizio;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.elis.retemonitor.modello.Scansione;
import org.elis.retemonitor.repository.ScansioneRepository;
import org.elis.retemonitor.scansione.InterfacciaRete;
import org.elis.retemonitor.scansione.RisultatoScansione;
import org.elis.retemonitor.scansione.ScannerRete;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;

/** Avvia le scansioni (automatiche ogni minuto o su richiesta) evitando che due partano insieme. */
@Service
public class ServizioScansioni {

    private static final Logger log = LoggerFactory.getLogger(ServizioScansioni.class);

    private final ScannerRete scanner;
    private final ServizioDispositivi servizioDispositivi;
    private final ScansioneRepository scansioneRepository;
    private final AtomicBoolean inCorso = new AtomicBoolean(false);
    private final ExecutorService esecutore = Executors.newSingleThreadExecutor();

    public ServizioScansioni(ScannerRete scanner, ServizioDispositivi servizioDispositivi,
                             ScansioneRepository scansioneRepository) {
        this.scanner = scanner;
        this.servizioDispositivi = servizioDispositivi;
        this.scansioneRepository = scansioneRepository;
    }

    @Scheduled(initialDelay = 3000, fixedDelayString = "${rete.intervallo-ms:60000}")
    public void scansioneAutomatica() {
        esegui();
    }

    /** Avvia una scansione senza bloccare la richiesta HTTP. Restituisce false se ce n'è già una in corso. */
    public boolean avviaInBackground() {
        if (inCorso.get()) return false;
        esecutore.submit(this::esegui);
        return true;
    }

    public boolean isInCorso() {
        return inCorso.get();
    }

    public Optional<Scansione> ultima() {
        return scansioneRepository.findTopByOrderByInizioDesc();
    }

    public InterfacciaRete interfaccia() {
        return scanner.interfaccia();
    }

    void esegui() {
        if (!inCorso.compareAndSet(false, true)) return;

        Scansione scansione = new Scansione();
        scansione.setInizio(LocalDateTime.now());
        scansione.setSottorete("-");
        try {
            RisultatoScansione risultato = scanner.scansiona();
            scansione.setSottorete(risultato.interfaccia().sottorete());
            scansione.setIndirizziControllati(risultato.indirizziControllati());
            scansione.setDispositiviTrovati(
                    servizioDispositivi.registraRisultati(risultato.host(), LocalDateTime.now()));
            log.info("Scansione di {} completata: {} dispositivi accesi",
                    risultato.interfaccia().sottorete(), risultato.host().size());
        } catch (RuntimeException e) {
            log.warn("Scansione non riuscita: {}", e.getMessage());
            String messaggio = String.valueOf(e.getMessage());
            scansione.setErrore(messaggio.length() > 500 ? messaggio.substring(0, 500) : messaggio);
        } finally {
            scansione.setFine(LocalDateTime.now());
            try {
                scansioneRepository.save(scansione);
            } finally {
                inCorso.set(false);
            }
        }
    }

    @PreDestroy
    void chiudi() {
        esecutore.shutdownNow();
    }
}
