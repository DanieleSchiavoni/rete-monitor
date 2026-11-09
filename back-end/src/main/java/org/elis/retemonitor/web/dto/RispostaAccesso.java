package org.elis.retemonitor.web.dto;

import java.time.Instant;

public record RispostaAccesso(String token, String username, Instant scadenza) {
}
