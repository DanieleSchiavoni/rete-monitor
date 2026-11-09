package org.elis.retemonitor.web.dto;

import java.time.LocalDateTime;

import org.elis.retemonitor.modello.CategoriaDispositivo;
import org.elis.retemonitor.modello.Dispositivo;

public record DispositivoDto(
        Long id,
        String nome,
        String nomePersonalizzato,
        String nomeHost,
        String indirizzoIp,
        String indirizzoMac,
        String produttore,
        boolean macCasuale,
        boolean questoComputer,
        boolean online,
        boolean fidato,
        CategoriaDispositivo categoria,
        String metodoRilevamento,
        LocalDateTime primaVolta,
        LocalDateTime ultimaVolta,
        LocalDateTime porteControllateIl) {

    public static DispositivoDto da(Dispositivo d) {
        return new DispositivoDto(d.getId(), d.nomeVisualizzato(), d.getNomePersonalizzato(), d.getNomeHost(),
                d.getIndirizzoIp(), d.getIndirizzoMac(), d.getProduttore(), d.isMacCasuale(), d.isQuestoComputer(),
                d.isOnline(), d.isFidato(), d.getCategoria(), d.getMetodoRilevamento(), d.getPrimaVolta(),
                d.getUltimaVolta(), d.getPorteControllateIl());
    }
}
