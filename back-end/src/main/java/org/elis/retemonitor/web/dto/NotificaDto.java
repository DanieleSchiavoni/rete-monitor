package org.elis.retemonitor.web.dto;

import java.time.LocalDateTime;

import org.elis.retemonitor.modello.TipoNotifica;

public record NotificaDto(Long id, TipoNotifica tipo, String messaggio, boolean letta, LocalDateTime creataIl,
                          Long dispositivoId) {
}
