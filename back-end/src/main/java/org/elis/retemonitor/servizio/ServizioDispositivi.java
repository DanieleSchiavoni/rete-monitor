package org.elis.retemonitor.servizio;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.elis.retemonitor.modello.CategoriaDispositivo;
import org.elis.retemonitor.modello.Dispositivo;
import org.elis.retemonitor.modello.EventoStato;
import org.elis.retemonitor.modello.PortaAperta;
import org.elis.retemonitor.modello.StatoDispositivo;
import org.elis.retemonitor.repository.DispositivoRepository;
import org.elis.retemonitor.repository.EventoStatoRepository;
import org.elis.retemonitor.repository.PortaApertaRepository;
import org.elis.retemonitor.scansione.ElencoProduttori;
import org.elis.retemonitor.scansione.HostRilevato;
import org.elis.retemonitor.scansione.ScannerPorte.PortaRilevata;
import org.elis.retemonitor.web.dto.ModificaDispositivoRichiesta;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServizioDispositivi {

    private static final String PREFISSO_CHIAVE_IP = "ip:";

    private final DispositivoRepository dispositivoRepository;
    private final EventoStatoRepository eventoRepository;
    private final PortaApertaRepository portaRepository;
    private final ServizioNotifiche servizioNotifiche;
    private final ElencoProduttori elencoProduttori;
    private final int scansioniMancatePerOffline;

    public ServizioDispositivi(DispositivoRepository dispositivoRepository,
                               EventoStatoRepository eventoRepository,
                               PortaApertaRepository portaRepository,
                               ServizioNotifiche servizioNotifiche,
                               ElencoProduttori elencoProduttori,
                               @Value("${rete.scansioni-mancate-per-offline:2}") int scansioniMancatePerOffline) {
        this.dispositivoRepository = dispositivoRepository;
        this.eventoRepository = eventoRepository;
        this.portaRepository = portaRepository;
        this.servizioNotifiche = servizioNotifiche;
        this.elencoProduttori = elencoProduttori;
        this.scansioniMancatePerOffline = Math.max(1, scansioniMancatePerOffline);
    }

    @Transactional(readOnly = true)
    public List<Dispositivo> elenco() {
        return dispositivoRepository.findAllByOrderByOnlineDescUltimaVoltaDesc();
    }

    @Transactional(readOnly = true)
    public Dispositivo dettaglio(Long id) {
        return dispositivoRepository.findById(id)
                .orElseThrow(() -> new ElementoNonTrovatoException("Dispositivo " + id + " non trovato"));
    }

    @Transactional(readOnly = true)
    public List<EventoStato> eventi(Long id) {
        dettaglio(id);
        return eventoRepository.findTop100ByDispositivoIdOrderByIstanteDesc(id);
    }

    /** Aggiorna solo i campi presenti nella richiesta (nome, categoria, fidato). */
    @Transactional
    public Dispositivo modifica(Long id, ModificaDispositivoRichiesta richiesta) {
        Dispositivo dispositivo = dettaglio(id);
        if (richiesta.nome() != null) {
            dispositivo.setNomePersonalizzato(richiesta.nome().isBlank() ? null : richiesta.nome().trim());
        }
        if (richiesta.categoria() != null) {
            dispositivo.setCategoria(richiesta.categoria());
        }
        if (richiesta.fidato() != null) {
            dispositivo.setFidato(richiesta.fidato());
            // Un dispositivo appena confermato non ha più bisogno delle sue notifiche
            if (richiesta.fidato()) servizioNotifiche.segnaLetteDelDispositivo(id);
        }
        return dispositivo;
    }

    /** Segna come fidati tutti i dispositivi conosciuti (utile dopo la prima scansione). */
    @Transactional
    public int segnaTuttiFidati() {
        int modificati = 0;
        for (Dispositivo dispositivo : dispositivoRepository.findAll()) {
            if (!dispositivo.isFidato()) {
                dispositivo.setFidato(true);
                modificati++;
            }
        }
        servizioNotifiche.segnaTutteLette();
        return modificati;
    }

    @Transactional
    public void elimina(Long id) {
        dispositivoRepository.delete(dettaglio(id));
    }

    @Transactional(readOnly = true)
    public List<PortaAperta> porte(Long id) {
        dettaglio(id);
        return portaRepository.findByDispositivoIdOrderByPortaAsc(id);
    }

    /** Sostituisce le porte salvate con quelle trovate nell'ultimo controllo. */
    @Transactional
    public List<PortaAperta> salvaPorte(Long id, List<PortaRilevata> trovate, LocalDateTime adesso) {
        Dispositivo dispositivo = dettaglio(id);
        portaRepository.eliminaDelDispositivo(id);
        List<PortaAperta> nuove = trovate.stream()
                .map(p -> new PortaAperta(dispositivo, p.porta(), p.servizio()))
                .toList();
        dispositivo.setPorteControllateIl(adesso);
        return portaRepository.saveAll(nuove);
    }

    /**
     * Aggiorna il database con il risultato di una scansione:
     * - i dispositivi nuovi vengono creati come accesi e generano una notifica;
     * - quelli già noti vengono aggiornati (IP, nome) e, se erano spenti, tornano accesi;
     * - quelli non trovati vengono segnati come spenti dopo N scansioni consecutive senza risposta.
     */
    @Transactional
    public int registraRisultati(List<HostRilevato> trovati, LocalDateTime adesso) {
        // Alla prima scansione tutti i dispositivi sono "nuovi": invece di una notifica per ognuno se ne crea una sola
        boolean primaScansione = dispositivoRepository.count() == 0;
        Set<Long> visti = new HashSet<>();

        for (HostRilevato host : trovati) {
            Ricerca ricerca = trovaOCrea(host, adesso);
            Dispositivo dispositivo = ricerca.dispositivo();
            dispositivo.setIndirizzoIp(host.indirizzoIp());
            if (host.nomeHost() != null) dispositivo.setNomeHost(host.nomeHost());
            if (host.indirizzoMac() != null) {
                dispositivo.setIndirizzoMac(host.indirizzoMac());
                dispositivo.setMacCasuale(ElencoProduttori.isMacCasuale(host.indirizzoMac()));
                String produttore = elencoProduttori.cerca(host.indirizzoMac());
                if (produttore != null) dispositivo.setProduttore(produttore);
            }
            dispositivo.setQuestoComputer(host.questoComputer());
            dispositivo.setMetodoRilevamento(host.metodo());
            dispositivo.setUltimaVolta(adesso);
            dispositivo.setScansioniMancate(0);
            if (!dispositivo.isOnline()) {
                dispositivo.setOnline(true);
                eventoRepository.save(new EventoStato(dispositivo, StatoDispositivo.ONLINE, adesso));
            }

            if (ricerca.nuovo()) {
                if (host.questoComputer()) {
                    dispositivo.setFidato(true);
                    dispositivo.setCategoria(CategoriaDispositivo.COMPUTER);
                } else if (!primaScansione) {
                    servizioNotifiche.nuovoDispositivo(dispositivo, adesso);
                }
            }
            visti.add(dispositivo.getId());
        }

        if (primaScansione && trovati.size() > 1) {
            servizioNotifiche.primaScansione(trovati.size(), adesso);
        }

        for (Dispositivo dispositivo : dispositivoRepository.findAll()) {
            if (visti.contains(dispositivo.getId()) || !dispositivo.isOnline()) continue;
            dispositivo.setScansioniMancate(dispositivo.getScansioniMancate() + 1);
            if (dispositivo.getScansioniMancate() >= scansioniMancatePerOffline) {
                dispositivo.setOnline(false);
                eventoRepository.save(new EventoStato(dispositivo, StatoDispositivo.OFFLINE, adesso));
            }
        }
        return trovati.size();
    }

    private Ricerca trovaOCrea(HostRilevato host, LocalDateTime adesso) {
        String chiaveIp = PREFISSO_CHIAVE_IP + host.indirizzoIp();
        String chiave = host.indirizzoMac() != null ? host.indirizzoMac() : chiaveIp;

        Optional<Dispositivo> esistente = dispositivoRepository.findByChiave(chiave);
        if (esistente.isPresent()) return new Ricerca(esistente.get(), false);

        // Visto prima senza MAC e ora con il MAC: si aggiorna la chiave invece di creare un doppione
        if (host.indirizzoMac() != null) {
            Optional<Dispositivo> senzaMac = dispositivoRepository.findByChiave(chiaveIp);
            if (senzaMac.isPresent()) {
                senzaMac.get().setChiave(chiave);
                return new Ricerca(senzaMac.get(), false);
            }
        }

        Dispositivo nuovo = new Dispositivo();
        nuovo.setChiave(chiave);
        nuovo.setIndirizzoIp(host.indirizzoIp());
        nuovo.setPrimaVolta(adesso);
        nuovo.setUltimaVolta(adesso);
        nuovo.setOnline(true);
        Dispositivo salvato = dispositivoRepository.save(nuovo);
        eventoRepository.save(new EventoStato(salvato, StatoDispositivo.ONLINE, adesso));
        return new Ricerca(salvato, true);
    }

    private record Ricerca(Dispositivo dispositivo, boolean nuovo) {
    }
}
