package org.elis.retemonitor.config;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.elis.retemonitor.scansione.ElencoProduttori;
import org.elis.retemonitor.scansione.LettoreTabellaArp;
import org.elis.retemonitor.scansione.RilevatoreInterfaccia;
import org.elis.retemonitor.scansione.ScannerPorte;
import org.elis.retemonitor.scansione.ScannerRete;
import org.elis.retemonitor.scansione.SistemaOperativo;
import org.elis.retemonitor.scansione.SondaHost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Crea gli oggetti della scansione. Le classi del package "scansione" non dipendono da Spring,
 * così si possono provare e riusare da sole.
 */
@Configuration
public class ConfigurazioneScansione {

    @Bean
    public SistemaOperativo sistemaOperativo() {
        return SistemaOperativo.corrente();
    }

    @Bean
    public SondaHost sondaHost(SistemaOperativo sistema,
                               @Value("${rete.timeout-ping-ms:1000}") int timeoutPingMs,
                               @Value("${rete.timeout-porta-ms:300}") int timeoutPortaMs,
                               @Value("${rete.porte:80,443,22,445}") String porte) {
        List<Integer> elencoPorte = Arrays.stream(porte.split(","))
                .map(String::trim)
                .filter(p -> !p.isEmpty())
                .map(Integer::valueOf)
                .toList();
        return new SondaHost(sistema, timeoutPingMs, timeoutPortaMs, elencoPorte);
    }

    @Bean
    public ScannerRete scannerRete(SistemaOperativo sistema, SondaHost sonda,
                                   @Value("${rete.interfaccia:}") String interfaccia,
                                   @Value("${rete.sottorete:}") String sottorete,
                                   @Value("${rete.thread:64}") int thread,
                                   @Value("${rete.arp-come-presenza:true}") boolean arpComePresenza) {
        return new ScannerRete(new RilevatoreInterfaccia(), new LettoreTabellaArp(), sonda, sistema,
                new ScannerRete.ImpostazioniScansione(interfaccia, sottorete, thread, arpComePresenza));
    }

    @Bean
    public ScannerPorte scannerPorte(@Value("${rete.timeout-porte-dettaglio-ms:700}") int timeoutMs) {
        return new ScannerPorte(timeoutMs);
    }

    @Bean
    public ElencoProduttori elencoProduttori(@Value("${rete.file-oui:oui.csv}") String fileOui) {
        return new ElencoProduttori(fileOui.isBlank() ? null : Path.of(fileOui));
    }
}
