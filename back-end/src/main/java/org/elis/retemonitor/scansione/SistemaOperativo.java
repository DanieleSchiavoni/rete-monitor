package org.elis.retemonitor.scansione;

import java.util.Locale;

/** Sistema operativo su cui gira il backend: cambia il comando di ping e il modo di leggere la tabella ARP. */
public enum SistemaOperativo {
    MAC, LINUX, WINDOWS, ALTRO;

    public static SistemaOperativo corrente() {
        String nome = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (nome.contains("mac")) return MAC;
        if (nome.contains("win")) return WINDOWS;
        if (nome.contains("nux") || nome.contains("nix")) return LINUX;
        return ALTRO;
    }
}
