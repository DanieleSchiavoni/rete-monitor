package org.elis.retemonitor.modello;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "porte_aperte")
public class PortaAperta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dispositivo_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Dispositivo dispositivo;

    @Column(nullable = false)
    private int porta;

    @Column(nullable = false, length = 60)
    private String servizio;

    protected PortaAperta() {
    }

    public PortaAperta(Dispositivo dispositivo, int porta, String servizio) {
        this.dispositivo = dispositivo;
        this.porta = porta;
        this.servizio = servizio;
    }

    public Long getId() { return id; }
    public Dispositivo getDispositivo() { return dispositivo; }
    public int getPorta() { return porta; }
    public String getServizio() { return servizio; }
}
