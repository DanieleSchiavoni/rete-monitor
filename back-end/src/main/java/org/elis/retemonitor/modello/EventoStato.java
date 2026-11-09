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

/** Registra quando un dispositivo si è acceso o spento: è lo storico mostrato nella pagina di dettaglio. */
@Entity
@Table(name = "eventi_stato")
public class EventoStato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dispositivo_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Dispositivo dispositivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatoDispositivo stato;

    @Column(nullable = false)
    private LocalDateTime istante;

    protected EventoStato() {
    }

    public EventoStato(Dispositivo dispositivo, StatoDispositivo stato, LocalDateTime istante) {
        this.dispositivo = dispositivo;
        this.stato = stato;
        this.istante = istante;
    }

    public Long getId() { return id; }
    public Dispositivo getDispositivo() { return dispositivo; }
    public StatoDispositivo getStato() { return stato; }
    public LocalDateTime getIstante() { return istante; }
}
