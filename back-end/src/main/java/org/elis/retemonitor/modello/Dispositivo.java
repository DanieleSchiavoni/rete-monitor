package org.elis.retemonitor.modello;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "dispositivi")
public class Dispositivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** MAC address, oppure "ip:<indirizzo>" se il MAC non è noto. */
    @Column(nullable = false, unique = true, length = 64)
    private String chiave;

    @Column(name = "indirizzo_mac", length = 17)
    private String indirizzoMac;

    @Column(name = "indirizzo_ip", nullable = false, length = 15)
    private String indirizzoIp;

    @Column(name = "nome_host")
    private String nomeHost;

    @Column(name = "nome_personalizzato", length = 100)
    private String nomePersonalizzato;

    private String produttore;

    @Column(name = "mac_casuale", nullable = false)
    private boolean macCasuale;

    @Column(name = "questo_computer", nullable = false)
    private boolean questoComputer;

    @Column(nullable = false)
    private boolean online;

    @Column(name = "scansioni_mancate", nullable = false)
    private int scansioniMancate;

    @Column(name = "metodo_rilevamento", length = 20)
    private String metodoRilevamento;

    @Column(name = "prima_volta", nullable = false)
    private LocalDateTime primaVolta;

    @Column(name = "ultima_volta", nullable = false)
    private LocalDateTime ultimaVolta;

    /** L'utente ha confermato di conoscere questo dispositivo. */
    @Column(nullable = false)
    private boolean fidato;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoriaDispositivo categoria = CategoriaDispositivo.SCONOSCIUTO;

    @Column(name = "porte_controllate_il")
    private LocalDateTime porteControllateIl;

    /** Nome da mostrare: quello scelto dall'utente, altrimenti il nome di rete, altrimenti l'IP. */
    public String nomeVisualizzato() {
        if (nomePersonalizzato != null && !nomePersonalizzato.isBlank()) return nomePersonalizzato;
        if (nomeHost != null && !nomeHost.isBlank()) return nomeHost;
        return indirizzoIp;
    }

    public Long getId() { return id; }
    public String getChiave() { return chiave; }
    public void setChiave(String chiave) { this.chiave = chiave; }
    public String getIndirizzoMac() { return indirizzoMac; }
    public void setIndirizzoMac(String indirizzoMac) { this.indirizzoMac = indirizzoMac; }
    public String getIndirizzoIp() { return indirizzoIp; }
    public void setIndirizzoIp(String indirizzoIp) { this.indirizzoIp = indirizzoIp; }
    public String getNomeHost() { return nomeHost; }
    public void setNomeHost(String nomeHost) { this.nomeHost = nomeHost; }
    public String getNomePersonalizzato() { return nomePersonalizzato; }
    public void setNomePersonalizzato(String nomePersonalizzato) { this.nomePersonalizzato = nomePersonalizzato; }
    public String getProduttore() { return produttore; }
    public void setProduttore(String produttore) { this.produttore = produttore; }
    public boolean isMacCasuale() { return macCasuale; }
    public void setMacCasuale(boolean macCasuale) { this.macCasuale = macCasuale; }
    public boolean isQuestoComputer() { return questoComputer; }
    public void setQuestoComputer(boolean questoComputer) { this.questoComputer = questoComputer; }
    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }
    public int getScansioniMancate() { return scansioniMancate; }
    public void setScansioniMancate(int scansioniMancate) { this.scansioniMancate = scansioniMancate; }
    public String getMetodoRilevamento() { return metodoRilevamento; }
    public void setMetodoRilevamento(String metodoRilevamento) { this.metodoRilevamento = metodoRilevamento; }
    public LocalDateTime getPrimaVolta() { return primaVolta; }
    public void setPrimaVolta(LocalDateTime primaVolta) { this.primaVolta = primaVolta; }
    public LocalDateTime getUltimaVolta() { return ultimaVolta; }
    public void setUltimaVolta(LocalDateTime ultimaVolta) { this.ultimaVolta = ultimaVolta; }
    public boolean isFidato() { return fidato; }
    public void setFidato(boolean fidato) { this.fidato = fidato; }
    public CategoriaDispositivo getCategoria() { return categoria; }
    public void setCategoria(CategoriaDispositivo categoria) { this.categoria = categoria; }
    public LocalDateTime getPorteControllateIl() { return porteControllateIl; }
    public void setPorteControllateIl(LocalDateTime porteControllateIl) { this.porteControllateIl = porteControllateIl; }
}
