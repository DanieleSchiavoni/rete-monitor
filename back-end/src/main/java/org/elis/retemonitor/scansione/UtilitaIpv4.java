package org.elis.retemonitor.scansione;

import java.util.ArrayList;
import java.util.List;

/** Calcoli sugli indirizzi IPv4 e sulle sottoreti in formato CIDR (es. 192.168.1.0/24). */
public final class UtilitaIpv4 {

    /** Prefisso minimo ammesso: oltre i 254 indirizzi di una /24 la scansione diventerebbe troppo lenta. */
    public static final int PREFISSO_MINIMO = 24;

    private UtilitaIpv4() {
    }

    public static int inIntero(String ip) {
        String[] parti = ip.trim().split("\\.");
        if (parti.length != 4) {
            throw new IllegalArgumentException("Indirizzo IPv4 non valido: " + ip);
        }
        int risultato = 0;
        for (String parte : parti) {
            int ottetto = Integer.parseInt(parte);
            if (ottetto < 0 || ottetto > 255) {
                throw new IllegalArgumentException("Indirizzo IPv4 non valido: " + ip);
            }
            risultato = (risultato << 8) | ottetto;
        }
        return risultato;
    }

    public static String inTesto(int ip) {
        return ((ip >>> 24) & 0xFF) + "." + ((ip >>> 16) & 0xFF) + "." + ((ip >>> 8) & 0xFF) + "." + (ip & 0xFF);
    }

    public static int maschera(int prefisso) {
        return prefisso == 0 ? 0 : 0xFFFFFFFF << (32 - prefisso);
    }

    /** Restituisce la sottorete CIDR che contiene l'indirizzo, limitata a una /24. */
    public static String sottorete(String ip, int prefisso) {
        int prefissoUsato = Math.max(prefisso, PREFISSO_MINIMO);
        int rete = inIntero(ip) & maschera(prefissoUsato);
        return inTesto(rete) + "/" + prefissoUsato;
    }

    /** Elenca gli indirizzi utilizzabili della sottorete (esclusi indirizzo di rete e broadcast). */
    public static List<String> indirizzi(String cidr) {
        String[] parti = cidr.trim().split("/");
        if (parti.length != 2) {
            throw new IllegalArgumentException("Sottorete non valida (atteso formato 192.168.1.0/24): " + cidr);
        }
        int prefisso = Integer.parseInt(parti[1]);
        if (prefisso < PREFISSO_MINIMO || prefisso > 30) {
            throw new IllegalArgumentException("Il prefisso deve essere tra /" + PREFISSO_MINIMO + " e /30: " + cidr);
        }
        int maschera = maschera(prefisso);
        int rete = inIntero(parti[0]) & maschera;
        int broadcast = rete | ~maschera;
        List<String> risultato = new ArrayList<>();
        for (int ip = rete + 1; ip < broadcast; ip++) {
            risultato.add(inTesto(ip));
        }
        return risultato;
    }

    public static boolean appartiene(String ip, String cidr) {
        String[] parti = cidr.split("/");
        int maschera = maschera(Integer.parseInt(parti[1]));
        return (inIntero(ip) & maschera) == (inIntero(parti[0]) & maschera);
    }
}
