package org.elis.retemonitor.scansione;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Ricava il produttore dai primi 3 byte del MAC (OUI).
 * L'elenco ufficiale è il file oui.csv dell'IEEE: https://standards-oui.ieee.org/oui/oui.csv
 * Se il file non c'è, il produttore resta vuoto e l'app funziona lo stesso.
 */
public class ElencoProduttori {

    private final Map<String, String> produttori = new HashMap<>();

    public ElencoProduttori(Path fileOui) {
        if (fileOui != null && Files.isReadable(fileOui)) {
            carica(fileOui);
        }
    }

    public int numeroProduttori() {
        return produttori.size();
    }

    public String cerca(String mac) {
        if (mac == null || mac.length() < 8) return null;
        String oui = mac.substring(0, 8).replace(":", "").toUpperCase(Locale.ROOT);
        return produttori.get(oui);
    }

    /**
     * Smartphone e PC recenti usano un MAC "privato" diverso per ogni rete Wi-Fi.
     * Si riconosce dal bit "amministrato localmente" (secondo carattere 2, 6, A o E).
     */
    public static boolean isMacCasuale(String mac) {
        if (mac == null || mac.length() < 2) return false;
        int primoByte = Integer.parseInt(mac.substring(0, 2), 16);
        return (primoByte & 0x02) != 0;
    }

    private void carica(Path fileOui) {
        try (BufferedReader lettore = Files.newBufferedReader(fileOui, StandardCharsets.UTF_8)) {
            lettore.readLine(); // intestazione: Registry,Assignment,Organization Name,Organization Address
            String riga;
            while ((riga = lettore.readLine()) != null) {
                List<String> campi = dividiCsv(riga);
                if (campi.size() >= 3 && campi.get(1).length() == 6) {
                    produttori.put(campi.get(1).toUpperCase(Locale.ROOT), campi.get(2).trim());
                }
            }
        } catch (IOException e) {
            produttori.clear();
        }
    }

    /** Divide una riga CSV rispettando i campi tra virgolette che contengono virgole. */
    static List<String> dividiCsv(String riga) {
        List<String> campi = new ArrayList<>();
        StringBuilder campo = new StringBuilder();
        boolean traVirgolette = false;
        for (int i = 0; i < riga.length(); i++) {
            char c = riga.charAt(i);
            if (c == '"') {
                if (traVirgolette && i + 1 < riga.length() && riga.charAt(i + 1) == '"') {
                    campo.append('"');
                    i++;
                } else {
                    traVirgolette = !traVirgolette;
                }
            } else if (c == ',' && !traVirgolette) {
                campi.add(campo.toString());
                campo.setLength(0);
            } else {
                campo.append(c);
            }
        }
        campi.add(campo.toString());
        return campi;
    }
}
