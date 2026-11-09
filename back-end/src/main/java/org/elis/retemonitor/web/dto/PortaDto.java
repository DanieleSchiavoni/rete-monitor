package org.elis.retemonitor.web.dto;

import org.elis.retemonitor.modello.PortaAperta;

public record PortaDto(int porta, String servizio) {

    public static PortaDto da(PortaAperta p) {
        return new PortaDto(p.getPorta(), p.getServizio());
    }
}
