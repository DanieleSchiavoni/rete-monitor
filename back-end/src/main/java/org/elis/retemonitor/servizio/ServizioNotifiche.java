package org.elis.retemonitor.servizio;

import java.time.LocalDateTime;
import java.util.List;

import org.elis.retemonitor.modello.Dispositivo;
import org.elis.retemonitor.modello.Notifica;
import org.elis.retemonitor.modello.TipoNotifica;
import org.elis.retemonitor.repository.NotificaRepository;
import org.elis.retemonitor.web.dto.NotificaDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServizioNotifiche {

    private final NotificaRepository repository;

    public ServizioNotifiche(NotificaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void nuovoDispositivo(Dispositivo dispositivo, LocalDateTime adesso) {
        String descrizione = dispositivo.getProduttore() != null
                ? dispositivo.getProduttore()
                : dispositivo.isMacCasuale() ? "MAC privato" : dispositivo.getIndirizzoMac();
        String nome = dispositivo.getNomeHost() != null ? dispositivo.getNomeHost() + " · " : "";
        String messaggio = "Nuovo dispositivo in rete: " + nome + dispositivo.getIndirizzoIp()
                + (descrizione != null ? " (" + descrizione + ")" : "");
        repository.save(new Notifica(dispositivo, TipoNotifica.NUOVO_DISPOSITIVO, messaggio, adesso));
    }

    @Transactional
    public void primaScansione(int dispositiviTrovati, LocalDateTime adesso) {
        String messaggio = "Prima scansione completata: " + dispositiviTrovati
                + " dispositivi trovati. Segna come fidati quelli che conosci, così vedrai subito quelli nuovi.";
        repository.save(new Notifica(null, TipoNotifica.PRIMA_SCANSIONE, messaggio, adesso));
    }

    /** Le notifiche vengono convertite in DTO dentro la transazione, per leggere l'id del dispositivo in sicurezza. */
    @Transactional(readOnly = true)
    public List<NotificaDto> elenco(boolean soloNonLette) {
        List<Notifica> notifiche = soloNonLette
                ? repository.findTop50ByLettaFalseOrderByCreataIlDesc()
                : repository.findTop50ByOrderByCreataIlDesc();
        return notifiche.stream().map(ServizioNotifiche::inDto).toList();
    }

    @Transactional(readOnly = true)
    public long conteggioNonLette() {
        return repository.countByLettaFalse();
    }

    @Transactional
    public void segnaLetta(Long id) {
        Notifica notifica = repository.findById(id)
                .orElseThrow(() -> new ElementoNonTrovatoException("Notifica " + id + " non trovata"));
        notifica.setLetta(true);
    }

    @Transactional
    public int segnaTutteLette() {
        return repository.segnaTutteLette();
    }

    @Transactional
    public int segnaLetteDelDispositivo(Long dispositivoId) {
        return repository.segnaLetteDelDispositivo(dispositivoId);
    }

    private static NotificaDto inDto(Notifica n) {
        Long dispositivoId = n.getDispositivo() != null ? n.getDispositivo().getId() : null;
        return new NotificaDto(n.getId(), n.getTipo(), n.getMessaggio(), n.isLetta(), n.getCreataIl(), dispositivoId);
    }
}
