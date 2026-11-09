package org.elis.retemonitor.servizio;

public class OperazioneNonConsentitaException extends RuntimeException {

    public OperazioneNonConsentitaException(String messaggio) {
        super(messaggio);
    }
}
