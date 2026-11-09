package org.elis.retemonitor.scansione;

/**
 * Scheda di rete del computer usata per la scansione.
 *
 * @param nome         nome di sistema (es. en0, wlan0)
 * @param indirizzoIp  IP di questo computer
 * @param indirizzoMac MAC di questo computer (può essere null)
 * @param sottorete    sottorete da scansionare in formato CIDR
 */
public record InterfacciaRete(String nome, String indirizzoIp, String indirizzoMac, String sottorete) {
}
