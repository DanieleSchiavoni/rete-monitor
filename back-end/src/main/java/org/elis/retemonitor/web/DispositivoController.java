package org.elis.retemonitor.web;

import java.util.List;
import java.util.Map;

import org.elis.retemonitor.servizio.ServizioDispositivi;
import org.elis.retemonitor.servizio.ServizioPorte;
import org.elis.retemonitor.web.dto.DispositivoDto;
import org.elis.retemonitor.web.dto.EventoDto;
import org.elis.retemonitor.web.dto.ModificaDispositivoRichiesta;
import org.elis.retemonitor.web.dto.PortaDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/dispositivi")
public class DispositivoController {

    private final ServizioDispositivi servizio;
    private final ServizioPorte servizioPorte;

    public DispositivoController(ServizioDispositivi servizio, ServizioPorte servizioPorte) {
        this.servizio = servizio;
        this.servizioPorte = servizioPorte;
    }

    @GetMapping
    public List<DispositivoDto> elenco() {
        return servizio.elenco().stream().map(DispositivoDto::da).toList();
    }

    @GetMapping("/{id}")
    public DispositivoDto dettaglio(@PathVariable Long id) {
        return DispositivoDto.da(servizio.dettaglio(id));
    }

    @GetMapping("/{id}/eventi")
    public List<EventoDto> eventi(@PathVariable Long id) {
        return servizio.eventi(id).stream().map(EventoDto::da).toList();
    }

    /** Modifica nome, categoria e/o fidato. I campi non inviati restano invariati. */
    @PatchMapping("/{id}")
    public DispositivoDto modifica(@PathVariable Long id, @Valid @RequestBody ModificaDispositivoRichiesta richiesta) {
        return DispositivoDto.da(servizio.modifica(id, richiesta));
    }

    @PostMapping("/fidati")
    public Map<String, Integer> segnaTuttiFidati() {
        return Map.of("modificati", servizio.segnaTuttiFidati());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> elimina(@PathVariable Long id) {
        servizio.elimina(id);
        return ResponseEntity.noContent().build();
    }

    /** Porte aperte trovate nell'ultimo controllo. */
    @GetMapping("/{id}/porte")
    public List<PortaDto> porte(@PathVariable Long id) {
        return servizio.porte(id).stream().map(PortaDto::da).toList();
    }

    /** Controlla adesso le porte del dispositivo (richiede qualche secondo). */
    @PostMapping("/{id}/porte")
    public List<PortaDto> controllaPorte(@PathVariable Long id) {
        return servizioPorte.controlla(id).stream().map(PortaDto::da).toList();
    }
}
