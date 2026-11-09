package org.elis.retemonitor.scansione;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/** Controlla se un singolo indirizzo IP risponde: prima con il ping, poi provando alcune porte TCP. */
public class SondaHost {

    private final SistemaOperativo sistema;
    private final int timeoutPingMs;
    private final int timeoutPortaMs;
    private final List<Integer> porte;

    public SondaHost(SistemaOperativo sistema, int timeoutPingMs, int timeoutPortaMs, List<Integer> porte) {
        this.sistema = sistema;
        this.timeoutPingMs = timeoutPingMs;
        this.timeoutPortaMs = timeoutPortaMs;
        this.porte = List.copyOf(porte);
    }

    /** Restituisce come ha risposto l'host ("PING" oppure "TCP:porta"), oppure vuoto se non ha risposto. */
    public Optional<String> sonda(String ip) {
        if (ping(ip)) {
            return Optional.of("PING");
        }
        for (int porta : porte) {
            if (portaRaggiungibile(ip, porta)) {
                return Optional.of("TCP:" + porta);
            }
        }
        return Optional.empty();
    }

    /**
     * Usa il comando ping del sistema: InetAddress.isReachable senza permessi di amministratore
     * non invia veri pacchetti ICMP su macOS e Linux.
     */
    boolean ping(String ip) {
        int secondi = Math.max(1, (int) Math.ceil(timeoutPingMs / 1000.0));
        List<String> comando = switch (sistema) {
            case WINDOWS -> List.of("ping", "-n", "1", "-w", String.valueOf(timeoutPingMs), ip);
            case MAC -> List.of("ping", "-c", "1", "-t", String.valueOf(secondi), ip);
            default -> List.of("ping", "-c", "1", "-W", String.valueOf(secondi), ip);
        };
        try {
            Process processo = new ProcessBuilder(comando).redirectErrorStream(true).start();
            String output = new String(processo.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (!processo.waitFor(secondi + 2L, TimeUnit.SECONDS)) {
                processo.destroyForcibly();
                return false;
            }
            if (processo.exitValue() != 0) return false;
            // Su Windows il ping può uscire con 0 anche con "host irraggiungibile": serve il TTL nella risposta
            return sistema != SistemaOperativo.WINDOWS || output.toUpperCase(Locale.ROOT).contains("TTL=");
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Una porta conta come "risposta" sia se la connessione riesce sia se viene rifiutata:
     * il rifiuto immediato dimostra comunque che il dispositivo è acceso.
     */
    boolean portaRaggiungibile(String ip, int porta) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, porta), timeoutPortaMs);
            return true;
        } catch (java.net.ConnectException e) {
            String messaggio = String.valueOf(e.getMessage()).toLowerCase(Locale.ROOT);
            return messaggio.contains("refused") || messaggio.contains("rifiutata");
        } catch (IOException e) {
            return false;
        }
    }

    /** Nome del dispositivo tramite DNS inverso (es. "iPhone-di-Daniele.local"), se il router lo conosce. */
    public String nomeHost(String ip) {
        try {
            String nome = InetAddress.getByName(ip).getCanonicalHostName();
            return nome == null || nome.equals(ip) ? null : nome;
        } catch (IOException e) {
            return null;
        }
    }
}
