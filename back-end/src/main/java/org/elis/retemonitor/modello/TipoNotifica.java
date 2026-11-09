package org.elis.retemonitor.modello;

public enum TipoNotifica {
    /** Un dispositivo mai visto prima è comparso in rete. */
    NUOVO_DISPOSITIVO,
    /** Riepilogo della prima scansione: invita a segnare come fidati i dispositivi conosciuti. */
    PRIMA_SCANSIONE
}
