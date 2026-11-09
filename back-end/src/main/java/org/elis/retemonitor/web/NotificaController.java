package org.elis.retemonitor.web;

import java.util.List;
import java.util.Map;

import org.elis.retemonitor.servizio.ServizioNotifiche;
import org.elis.retemonitor.web.dto.NotificaDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifiche")
public class NotificaController {

    private final ServizioNotifiche servizio;

    public NotificaController(ServizioNotifiche servizio) {
        this.servizio = servizio;
    }

    @GetMapping
    public List<NotificaDto> elenco(@RequestParam(defaultValue = "false") boolean soloNonLette) {
        return servizio.elenco(soloNonLette);
    }

    @GetMapping("/conteggio")
    public Map<String, Long> conteggio() {
        return Map.of("nonLette", servizio.conteggioNonLette());
    }

    @PostMapping("/{id}/letta")
    public ResponseEntity<Void> segnaLetta(@PathVariable Long id) {
        servizio.segnaLetta(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/lette")
    public Map<String, Integer> segnaTutteLette() {
        return Map.of("modificate", servizio.segnaTutteLette());
    }
}
