package org.elis.retemonitor.scansione;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Legge la tabella ARP del sistema operativo, cioè l'elenco IP → MAC dei dispositivi
 * con cui il computer ha comunicato di recente nella rete locale.
 */
public class LettoreTabellaArp {

    private static final Pattern IPV4 = Pattern.compile("\\b(\\d{1,3}(?:\\.\\d{1,3}){3})\\b");
    private static final Pattern MAC = Pattern.compile("\\b([0-9a-fA-F]{1,2}(?:[:-][0-9a-fA-F]{1,2}){5})\\b");

    /** Restituisce la tabella ARP come mappa IP → MAC (MAC in minuscolo, separato da ':'). */
    public Map<String, String> leggi(SistemaOperativo sistema) {
        try {
            String testo = switch (sistema) {
                case LINUX -> leggiFileOComando(Path.of("/proc/net/arp"), "ip", "neigh");
                case WINDOWS -> eseguiComando("arp", "-a");
                default -> eseguiComando("arp", "-an");
            };
            return analizza(testo);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return Map.of();
        }
    }

    /**
     * Estrae le coppie IP/MAC da un testo. Funziona con il formato di macOS ({@code arp -an}),
     * Linux ({@code /proc/net/arp}, {@code ip neigh}) e Windows ({@code arp -a}).
     */
    public static Map<String, String> analizza(String testo) {
        Map<String, String> risultato = new LinkedHashMap<>();
        for (String riga : testo.split("\\R")) {
            String minuscola = riga.toLowerCase(Locale.ROOT);
            if (minuscola.contains("incomplete") || minuscola.contains("failed")) continue;

            Matcher ip = IPV4.matcher(riga);
            Matcher mac = MAC.matcher(riga);
            if (ip.find() && mac.find()) {
                String macNormalizzato = normalizzaMac(mac.group(1));
                if (macValido(macNormalizzato)) {
                    risultato.put(ip.group(1), macNormalizzato);
                }
            }
        }
        return risultato;
    }

    /** Converte "0:1e:c2:a:b:c" o "00-1E-C2-0A-0B-0C" in "00:1e:c2:0a:0b:0c". */
    public static String normalizzaMac(String mac) {
        String[] parti = mac.toLowerCase(Locale.ROOT).split("[:-]");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parti.length; i++) {
            if (i > 0) sb.append(':');
            sb.append(parti[i].length() == 1 ? "0" + parti[i] : parti[i]);
        }
        return sb.toString();
    }

    private static boolean macValido(String mac) {
        if (mac.equals("00:00:00:00:00:00") || mac.equals("ff:ff:ff:ff:ff:ff")) return false;
        // Il bit meno significativo del primo byte a 1 indica un indirizzo multicast, non un dispositivo
        int primoByte = Integer.parseInt(mac.substring(0, 2), 16);
        return (primoByte & 0x01) == 0;
    }

    private static String leggiFileOComando(Path file, String... comando) throws IOException, InterruptedException {
        if (Files.isReadable(file)) {
            return Files.readString(file, StandardCharsets.UTF_8);
        }
        return eseguiComando(comando);
    }

    private static String eseguiComando(String... comando) throws IOException, InterruptedException {
        Process processo = new ProcessBuilder(comando).redirectErrorStream(true).start();
        byte[] output = processo.getInputStream().readAllBytes();
        processo.waitFor(5, TimeUnit.SECONDS);
        return new String(output, StandardCharsets.UTF_8);
    }
}
