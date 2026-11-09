package org.elis.retemonitor.web.dto;

import java.time.LocalDateTime;

import org.elis.retemonitor.modello.EventoStato;
import org.elis.retemonitor.modello.StatoDispositivo;

public record EventoDto(Long id, StatoDispositivo stato, LocalDateTime istante) {

    public static EventoDto da(EventoStato e) {
        return new EventoDto(e.getId(), e.getStato(), e.getIstante());
    }
}
