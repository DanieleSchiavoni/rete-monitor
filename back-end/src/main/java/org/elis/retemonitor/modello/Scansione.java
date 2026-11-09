package org.elis.retemonitor.modello;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "scansioni")
public class Scansione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime inizio;

    private LocalDateTime fine;

    @Column(nullable = false, length = 18)
    private String sottorete;

    @Column(name = "indirizzi_controllati", nullable = false)
    private int indirizziControllati;

    @Column(name = "dispositivi_trovati", nullable = false)
    private int dispositiviTrovati;

    @Column(length = 500)
    private String errore;

    public Long getId() { return id; }
    public LocalDateTime getInizio() { return inizio; }
    public void setInizio(LocalDateTime inizio) { this.inizio = inizio; }
    public LocalDateTime getFine() { return fine; }
    public void setFine(LocalDateTime fine) { this.fine = fine; }
    public String getSottorete() { return sottorete; }
    public void setSottorete(String sottorete) { this.sottorete = sottorete; }
    public int getIndirizziControllati() { return indirizziControllati; }
    public void setIndirizziControllati(int indirizziControllati) { this.indirizziControllati = indirizziControllati; }
    public int getDispositiviTrovati() { return dispositiviTrovati; }
    public void setDispositiviTrovati(int dispositiviTrovati) { this.dispositiviTrovati = dispositiviTrovati; }
    public String getErrore() { return errore; }
    public void setErrore(String errore) { this.errore = errore; }
}
