package org.elis.retemonitor.web;

import java.util.Map;

import org.elis.retemonitor.servizio.CredenzialiNonValideException;
import org.elis.retemonitor.servizio.ElementoNonTrovatoException;
import org.elis.retemonitor.servizio.OperazioneNonConsentitaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GestoreErrori {

    @ExceptionHandler(ElementoNonTrovatoException.class)
    public ResponseEntity<Map<String, String>> nonTrovato(ElementoNonTrovatoException e) {
        return risposta(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(CredenzialiNonValideException.class)
    public ResponseEntity<Map<String, String>> credenzialiNonValide(CredenzialiNonValideException e) {
        return risposta(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(OperazioneNonConsentitaException.class)
    public ResponseEntity<Map<String, String>> nonConsentita(OperazioneNonConsentitaException e) {
        return risposta(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> nonValido(MethodArgumentNotValidException e) {
        String messaggio = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(errore -> errore.getDefaultMessage())
                .orElse("Richiesta non valida");
        return risposta(HttpStatus.BAD_REQUEST, messaggio);
    }

    private static ResponseEntity<Map<String, String>> risposta(HttpStatus stato, String messaggio) {
        return ResponseEntity.status(stato).body(Map.of("messaggio", messaggio));
    }
}
