package org.elis.retemonitor.scansione;

import java.util.List;

public record RisultatoScansione(InterfacciaRete interfaccia, int indirizziControllati, List<HostRilevato> host) {
}
