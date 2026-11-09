package org.elis.retemonitor.servizio;

public class ElementoNonTrovatoException extends RuntimeException {

    public ElementoNonTrovatoException(String messaggio) {
        super(messaggio);
    }
}
