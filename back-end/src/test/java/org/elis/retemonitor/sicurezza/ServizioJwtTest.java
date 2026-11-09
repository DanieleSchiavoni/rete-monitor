package org.elis.retemonitor.sicurezza;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class ServizioJwtTest {

    private static final String SEGRETO = "una-chiave-di-prova-lunga-almeno-32-caratteri";

    @Test
    void ilTokenCreatoVieneRiconosciuto() {
        ServizioJwt servizio = new ServizioJwt(SEGRETO, Duration.ofHours(1));
        String token = servizio.crea("daniele").token();
        assertEquals(Optional.of("daniele"), servizio.verifica(token));
    }

    @Test
    void unTokenFirmatoConUnAltraChiaveVieneRifiutato() {
        String token = new ServizioJwt(SEGRETO, Duration.ofHours(1)).crea("daniele").token();
        ServizioJwt altro = new ServizioJwt("un-altra-chiave-di-prova-lunga-32-caratteri", Duration.ofHours(1));
        assertTrue(altro.verifica(token).isEmpty());
    }

    @Test
    void unTokenScadutoVieneRifiutato() {
        ServizioJwt servizio = new ServizioJwt(SEGRETO, Duration.ofSeconds(-10));
        String token = servizio.crea("daniele").token();
        assertTrue(servizio.verifica(token).isEmpty());
    }

    @Test
    void unTestoQualsiasiVieneRifiutato() {
        ServizioJwt servizio = new ServizioJwt(SEGRETO, Duration.ofHours(1));
        assertTrue(servizio.verifica("non-sono-un-token").isEmpty());
    }

    @Test
    void unaChiaveTroppoCortaBloccaLAvvio() {
        assertThrows(IllegalStateException.class, () -> new ServizioJwt("corta", Duration.ofHours(1)));
    }

    @Test
    void senzaChiaveNeGeneraUnaCasuale() {
        ServizioJwt servizio = new ServizioJwt("", Duration.ofHours(1));
        assertEquals(Optional.of("daniele"), servizio.verifica(servizio.crea("daniele").token()));
    }
}
