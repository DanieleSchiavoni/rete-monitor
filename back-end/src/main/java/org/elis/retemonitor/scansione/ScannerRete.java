package org.elis.retemonitor.scansione;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Esegue una scansione completa della sottorete:
 * 1. trova la scheda di rete e la sottorete;
 * 2. prova in parallelo tutti gli indirizzi (ping, poi porte TCP);
 * 3. legge la tabella ARP per avere i MAC (si riempie proprio grazie ai tentativi del punto 2);
 * 4. unisce i risultati.
 */
public class ScannerRete {

    private final RilevatoreInterfaccia rilevatoreInterfaccia;
    private final LettoreTabellaArp lettoreArp;
    private final SondaHost sonda;
    private final SistemaOperativo sistema;
    private final ImpostazioniScansione impostazioni;

    public ScannerRete(RilevatoreInterfaccia rilevatoreInterfaccia, LettoreTabellaArp lettoreArp, SondaHost sonda,
                       SistemaOperativo sistema, ImpostazioniScansione impostazioni) {
        this.rilevatoreInterfaccia = rilevatoreInterfaccia;
        this.lettoreArp = lettoreArp;
        this.sonda = sonda;
        this.sistema = sistema;
        this.impostazioni = impostazioni;
    }

    public InterfacciaRete interfaccia() {
        return rilevatoreInterfaccia.rileva(impostazioni.interfaccia(), impostazioni.sottorete());
    }

    public RisultatoScansione scansiona() {
        InterfacciaRete interfaccia = interfaccia();
        List<String> indirizzi = UtilitaIpv4.indirizzi(interfaccia.sottorete());

        List<Sondaggio> sondaggi = new ArrayList<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(impostazioni.thread())) {
            List<Future<Sondaggio>> futuri = new ArrayList<>();
            for (String ip : indirizzi) {
                if (ip.equals(interfaccia.indirizzoIp())) continue;
                futuri.add(pool.submit(() -> new Sondaggio(ip, sonda.sonda(ip))));
            }
            for (Future<Sondaggio> futuro : futuri) {
                sondaggi.add(attendi(futuro));
            }

            Map<String, String> tabellaArp = lettoreArp.leggi(sistema);

            List<Future<HostRilevato>> risultatiFuturi = new ArrayList<>();
            for (Sondaggio s : sondaggi) {
                String mac = tabellaArp.get(s.ip);
                boolean presenteInArp = mac != null && impostazioni.arpComePresenza();
                if (s.risposta.isEmpty() && !presenteInArp) continue;

                String metodo = s.risposta.orElse("ARP");
                risultatiFuturi.add(pool.submit(() -> new HostRilevato(s.ip, mac, sonda.nomeHost(s.ip), metodo, false)));
            }

            List<HostRilevato> trovati = new ArrayList<>();
            trovati.add(new HostRilevato(interfaccia.indirizzoIp(), interfaccia.indirizzoMac(),
                    sonda.nomeHost(interfaccia.indirizzoIp()), "LOCALE", true));
            for (Future<HostRilevato> futuro : risultatiFuturi) {
                trovati.add(attendi(futuro));
            }
            trovati.sort(Comparator.comparingLong(h -> Integer.toUnsignedLong(UtilitaIpv4.inIntero(h.indirizzoIp()))));
            return new RisultatoScansione(interfaccia, indirizzi.size(), trovati);
        }
    }

    private static <T> T attendi(Future<T> futuro) {
        try {
            return futuro.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Scansione interrotta", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Errore durante la scansione: " + e.getCause().getMessage(), e.getCause());
        }
    }

    private record Sondaggio(String ip, Optional<String> risposta) {
    }

    public record ImpostazioniScansione(String interfaccia, String sottorete, int thread, boolean arpComePresenza) {
    }
}
