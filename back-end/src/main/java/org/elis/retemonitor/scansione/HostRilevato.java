package org.elis.retemonitor.scansione;

/**
 * Dispositivo trovato acceso durante una scansione.
 *
 * @param metodo come è stato rilevato: PING, TCP:porta, ARP oppure LOCALE (questo computer)
 */
public record HostRilevato(String indirizzoIp, String indirizzoMac, String nomeHost, String metodo,
                           boolean questoComputer) {
}
