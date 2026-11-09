package org.elis.retemonitor.servizio;

import java.time.LocalDateTime;
import java.util.List;

import org.elis.retemonitor.modello.Dispositivo;
import org.elis.retemonitor.modello.PortaAperta;
import org.elis.retemonitor.scansione.ScannerPorte;
import org.elis.retemonitor.scansione.ScannerPorte.PortaRilevata;
import org.springframework.stereotype.Service;

/**
 * Controlla le porte di un dispositivo. Non è transazionale di proposito: il controllo dura
 * qualche secondo e non deve tenere aperta una connessione al database nel frattempo.
 */
@Service
public class ServizioPorte {

    private final ServizioDispositivi servizioDispositivi;
    private final ScannerPorte scannerPorte;

    public ServizioPorte(ServizioDispositivi servizioDispositivi, ScannerPorte scannerPorte) {
        this.servizioDispositivi = servizioDispositivi;
        this.scannerPorte = scannerPorte;
    }

    public List<PortaAperta> controlla(Long id) {
        Dispositivo dispositivo = servizioDispositivi.dettaglio(id);
        List<PortaRilevata> aperte = scannerPorte.scansiona(dispositivo.getIndirizzoIp());
        return servizioDispositivi.salvaPorte(id, aperte, LocalDateTime.now());
    }
}
