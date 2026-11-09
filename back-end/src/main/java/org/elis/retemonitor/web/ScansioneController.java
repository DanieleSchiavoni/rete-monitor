package org.elis.retemonitor.web;

import org.elis.retemonitor.modello.Scansione;
import org.elis.retemonitor.scansione.InterfacciaRete;
import org.elis.retemonitor.servizio.ServizioScansioni;
import org.elis.retemonitor.web.dto.InfoReteDto;
import org.elis.retemonitor.web.dto.StatoScansioneDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ScansioneController {

    private final ServizioScansioni servizio;

    public ScansioneController(ServizioScansioni servizio) {
        this.servizio = servizio;
    }

    /** Avvia subito una scansione. Risponde 202 se è partita, 409 se ce n'era già una in corso. */
    @PostMapping("/scansioni")
    public ResponseEntity<StatoScansioneDto> avvia() {
        boolean avviata = servizio.avviaInBackground();
        return ResponseEntity.status(avviata ? HttpStatus.ACCEPTED : HttpStatus.CONFLICT).body(stato());
    }

    @GetMapping("/scansioni/stato")
    public StatoScansioneDto stato() {
        boolean inCorso = servizio.isInCorso();
        return servizio.ultima()
                .map(s -> daScansione(inCorso, s))
                .orElse(new StatoScansioneDto(inCorso, null, null, null, null, null, null));
    }

    @GetMapping("/rete")
    public InfoReteDto rete() {
        try {
            InterfacciaRete i = servizio.interfaccia();
            return new InfoReteDto(i.nome(), i.indirizzoIp(), i.indirizzoMac(), i.sottorete(), null);
        } catch (RuntimeException e) {
            return new InfoReteDto(null, null, null, null, e.getMessage());
        }
    }

    private static StatoScansioneDto daScansione(boolean inCorso, Scansione s) {
        return new StatoScansioneDto(inCorso, s.getInizio(), s.getFine(), s.getSottorete(),
                s.getIndirizziControllati(), s.getDispositiviTrovati(), s.getErrore());
    }
}
