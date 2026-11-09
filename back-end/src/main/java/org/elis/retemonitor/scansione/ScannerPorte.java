package org.elis.retemonitor.scansione;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Controlla quali porte TCP comuni sono aperte su un dispositivo.
 * Il servizio associato alla porta aiuta a capire che cos'è (stampante, telecamera, NAS...).
 */
public class ScannerPorte {

    /** Porte controllate e servizio che di solito le usa. */
    public static final Map<Integer, String> SERVIZI_NOTI = creaServiziNoti();

    private final int timeoutMs;

    public ScannerPorte(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public List<PortaRilevata> scansiona(String ip) {
        List<PortaRilevata> aperte = new ArrayList<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(Math.min(32, SERVIZI_NOTI.size()))) {
            List<Future<PortaRilevata>> futuri = new ArrayList<>();
            for (Map.Entry<Integer, String> voce : SERVIZI_NOTI.entrySet()) {
                futuri.add(pool.submit(() -> aperta(ip, voce.getKey()) ? new PortaRilevata(voce.getKey(), voce.getValue()) : null));
            }
            for (Future<PortaRilevata> futuro : futuri) {
                PortaRilevata porta = futuro.get();
                if (porta != null) aperte.add(porta);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Controllo delle porte interrotto", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Errore nel controllo delle porte: " + e.getCause().getMessage(), e);
        }
        aperte.sort(Comparator.comparingInt(PortaRilevata::porta));
        return aperte;
    }

    /** Qui conta solo la connessione riuscita: una porta che rifiuta la connessione è chiusa. */
    boolean aperta(String ip, int porta) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, porta), timeoutMs);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static Map<Integer, String> creaServiziNoti() {
        Map<Integer, String> servizi = new LinkedHashMap<>();
        servizi.put(21, "FTP (trasferimento file)");
        servizi.put(22, "SSH (accesso remoto)");
        servizi.put(23, "Telnet (accesso remoto non cifrato)");
        servizi.put(53, "DNS");
        servizi.put(80, "HTTP (pagina web)");
        servizi.put(139, "NetBIOS (condivisione Windows)");
        servizi.put(443, "HTTPS (pagina web sicura)");
        servizi.put(445, "SMB (cartelle condivise)");
        servizi.put(515, "LPD (stampa)");
        servizi.put(548, "AFP (condivisione Apple)");
        servizi.put(554, "RTSP (video, telecamere)");
        servizi.put(631, "IPP (stampa)");
        servizi.put(1883, "MQTT (domotica)");
        servizi.put(3306, "MySQL (database)");
        servizi.put(3389, "Desktop remoto Windows");
        servizi.put(5000, "UPnP / pannello NAS");
        servizi.put(5001, "Pannello NAS (HTTPS)");
        servizi.put(5900, "VNC (schermo remoto)");
        servizi.put(7000, "AirPlay");
        servizi.put(8008, "Chromecast / Google Cast");
        servizi.put(8009, "Chromecast / Google Cast");
        servizi.put(8080, "HTTP alternativo");
        servizi.put(8443, "HTTPS alternativo");
        servizi.put(9100, "Stampa diretta (RAW)");
        servizi.put(32400, "Plex Media Server");
        servizi.put(49152, "UPnP");
        servizi.put(62078, "Sincronizzazione iPhone/iPad");
        return Map.copyOf(servizi);
    }

    public record PortaRilevata(int porta, String servizio) {
    }
}
