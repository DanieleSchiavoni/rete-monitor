package org.elis.retemonitor.modello;

import java.time.LocalDateTime;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "notifiche")
public class Notifica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Dispositivo a cui si riferisce (null per le notifiche generali). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispositivo_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Dispositivo dispositivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoNotifica tipo;

    @Column(nullable = false, length = 300)
    private String messaggio;

    @Column(nullable = false)
    private boolean letta;

    @Column(name = "creata_il", nullable = false)
    private LocalDateTime creataIl;

    protected Notifica() {
    }

    public Notifica(Dispositivo dispositivo, TipoNotifica tipo, String messaggio, LocalDateTime creataIl) {
        this.dispositivo = dispositivo;
        this.tipo = tipo;
        this.messaggio = messaggio.length() > 300 ? messaggio.substring(0, 300) : messaggio;
        this.creataIl = creataIl;
    }

    public Long getId() { return id; }
    public Dispositivo getDispositivo() { return dispositivo; }
    public TipoNotifica getTipo() { return tipo; }
    public String getMessaggio() { return messaggio; }
    public boolean isLetta() { return letta; }
    public void setLetta(boolean letta) { this.letta = letta; }
    public LocalDateTime getCreataIl() { return creataIl; }
}
