package org.elis.retemonitor.scansione;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ScansioneTest {

    @Test
    void leggeLaTabellaArpDiMacOs() {
        String testo = """
                ? (192.168.1.1) at a4:91:b1:2:3:4 on en0 ifscope [ethernet]
                ? (192.168.1.5) at (incomplete) on en0 ifscope [ethernet]
                ? (192.168.1.255) at ff:ff:ff:ff:ff:ff on en0 ifscope [ethernet]
                ? (224.0.0.251) at 1:0:5e:0:0:fb on en0 ifscope permanent [ethernet]
                """;
        assertEquals(Map.of("192.168.1.1", "a4:91:b1:02:03:04"), LettoreTabellaArp.analizza(testo));
    }

    @Test
    void leggeLaTabellaArpDiLinux() {
        String testo = """
                IP address       HW type     Flags       HW address            Mask     Device
                192.168.1.1      0x1         0x2         aa:bb:cc:dd:ee:01     *        wlan0
                192.168.1.9      0x1         0x0         00:00:00:00:00:00     *        wlan0
                """;
        assertEquals(Map.of("192.168.1.1", "aa:bb:cc:dd:ee:01"), LettoreTabellaArp.analizza(testo));
    }

    @Test
    void leggeLaTabellaArpDiWindows() {
        String testo = """
                Interfaccia: 192.168.1.10 --- 0x5
                  Indirizzo Internet    Indirizzo fisico      Tipo
                  192.168.1.1           aa-bb-cc-dd-ee-02     dinamico
                  192.168.1.255         ff-ff-ff-ff-ff-ff     statico
                """;
        assertEquals(Map.of("192.168.1.1", "aa:bb:cc:dd:ee:02"), LettoreTabellaArp.analizza(testo));
    }

    @Test
    void calcolaLaSottoreteLimitandolaAUnaSlash24() {
        assertEquals("192.168.1.0/24", UtilitaIpv4.sottorete("192.168.1.37", 24));
        assertEquals("10.0.5.0/24", UtilitaIpv4.sottorete("10.0.5.9", 16));
    }

    @Test
    void elencaGliIndirizziSenzaReteEBroadcast() {
        List<String> indirizzi = UtilitaIpv4.indirizzi("192.168.1.0/24");
        assertEquals(254, indirizzi.size());
        assertEquals("192.168.1.1", indirizzi.get(0));
        assertEquals("192.168.1.254", indirizzi.get(253));
    }

    @Test
    void riconosceIMacCasuali() {
        assertTrue(ElencoProduttori.isMacCasuale("3a:1b:c2:d4:e5:f6"));
        assertFalse(ElencoProduttori.isMacCasuale("a4:91:b1:02:03:04"));
    }

    @Test
    void divideLeRigheCsvConVirgolette() {
        List<String> campi = ElencoProduttori.dividiCsv("MA-L,A491B1,\"Azienda, S.p.A.\",Via Roma 1");
        assertEquals(List.of("MA-L", "A491B1", "Azienda, S.p.A.", "Via Roma 1"), campi);
    }
}
