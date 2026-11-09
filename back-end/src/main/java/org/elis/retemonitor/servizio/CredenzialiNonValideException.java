package org.elis.retemonitor.servizio;

public class CredenzialiNonValideException extends RuntimeException {

    public CredenzialiNonValideException() {
        super("Username o password non corretti");
    }
}
