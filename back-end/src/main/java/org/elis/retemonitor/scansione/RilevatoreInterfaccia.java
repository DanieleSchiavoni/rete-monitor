package org.elis.retemonitor.scansione;

import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Trova la scheda di rete collegata alla LAN (Wi-Fi o cavo) e la relativa sottorete. */
public class RilevatoreInterfaccia {

    /** Interfacce virtuali da ignorare: VPN, Docker, macchine virtuali, AirDrop... */
    private static final List<String> PREFISSI_DA_IGNORARE = List.of(
            "utun", "tun", "tap", "docker", "veth", "br-", "vbox", "vmnet", "virbr",
            "awdl", "llw", "bridge", "zt", "tailscale", "wg", "ham");

    /** Interfacce da preferire se ce ne sono più di una. */
    private static final List<String> PREFISSI_PREFERITI = List.of("en0", "wlan", "wlp", "wi-fi", "wifi", "en", "eth");

    /**
     * @param nomeForzato      nome dell'interfaccia scelto in configurazione, oppure vuoto
     * @param sottoreteForzata sottorete CIDR scelta in configurazione, oppure vuota
     */
    public InterfacciaRete rileva(String nomeForzato, String sottoreteForzata) {
        List<Candidata> candidate = trovaCandidate();
        if (candidate.isEmpty()) {
            throw new IllegalStateException("Nessuna scheda di rete collegata a una rete locale (Wi-Fi o cavo)");
        }

        Candidata scelta;
        if (nomeForzato != null && !nomeForzato.isBlank()) {
            scelta = candidate.stream()
                    .filter(c -> c.nome.equalsIgnoreCase(nomeForzato.trim()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Interfaccia '" + nomeForzato + "' non trovata. Disponibili: "
                                    + candidate.stream().map(c -> c.nome).toList()));
        } else {
            candidate.sort(Comparator.comparingInt(c -> priorita(c.nome)));
            scelta = candidate.get(0);
        }

        String sottorete = (sottoreteForzata != null && !sottoreteForzata.isBlank())
                ? sottoreteForzata.trim()
                : UtilitaIpv4.sottorete(scelta.ip, scelta.prefisso);
        return new InterfacciaRete(scelta.nome, scelta.ip, scelta.mac, sottorete);
    }

    private List<Candidata> trovaCandidate() {
        List<Candidata> risultato = new ArrayList<>();
        try {
            for (NetworkInterface interfaccia : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!interfaccia.isUp() || interfaccia.isLoopback() || interfaccia.isVirtual()
                        || daIgnorare(interfaccia.getName())) {
                    continue;
                }
                for (InterfaceAddress indirizzo : interfaccia.getInterfaceAddresses()) {
                    if (indirizzo.getAddress() instanceof Inet4Address ipv4 && ipv4.isSiteLocalAddress()) {
                        risultato.add(new Candidata(interfaccia.getName(), ipv4.getHostAddress(),
                                indirizzo.getNetworkPrefixLength(), formattaMac(interfaccia.getHardwareAddress())));
                    }
                }
            }
        } catch (SocketException e) {
            throw new IllegalStateException("Impossibile leggere le schede di rete: " + e.getMessage(), e);
        }
        return risultato;
    }

    private static boolean daIgnorare(String nome) {
        String minuscolo = nome.toLowerCase(Locale.ROOT);
        return PREFISSI_DA_IGNORARE.stream().anyMatch(minuscolo::startsWith);
    }

    private static int priorita(String nome) {
        String minuscolo = nome.toLowerCase(Locale.ROOT);
        for (int i = 0; i < PREFISSI_PREFERITI.size(); i++) {
            if (minuscolo.startsWith(PREFISSI_PREFERITI.get(i))) return i;
        }
        return PREFISSI_PREFERITI.size();
    }

    static String formattaMac(byte[] byteMac) {
        if (byteMac == null || byteMac.length != 6) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < byteMac.length; i++) {
            if (i > 0) sb.append(':');
            sb.append(String.format("%02x", byteMac[i]));
        }
        return sb.toString();
    }

    private record Candidata(String nome, String ip, int prefisso, String mac) {
    }
}
