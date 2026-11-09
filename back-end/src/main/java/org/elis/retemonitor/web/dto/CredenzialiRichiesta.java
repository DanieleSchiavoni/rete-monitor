package org.elis.retemonitor.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CredenzialiRichiesta(
        @NotBlank(message = "Inserisci l'username")
        @Size(min = 3, max = 50, message = "L'username deve avere tra 3 e 50 caratteri")
        String username,
        @NotBlank(message = "Inserisci la password")
        @Size(min = 8, max = 100, message = "La password deve avere almeno 8 caratteri")
        String password) {
}
