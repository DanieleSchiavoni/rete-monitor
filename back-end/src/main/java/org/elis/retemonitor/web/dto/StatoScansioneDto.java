package org.elis.retemonitor.web.dto;

import java.time.LocalDateTime;

/** Stato delle scansioni: se ce n'è una in corso e com'è andata l'ultima (i campi dell'ultima possono essere null). */
public record StatoScansioneDto(
        boolean inCorso,
        LocalDateTime inizio,
        LocalDateTime fine,
        String sottorete,
        Integer indirizziControllati,
        Integer dispositiviTrovati,
        String errore) {
}
