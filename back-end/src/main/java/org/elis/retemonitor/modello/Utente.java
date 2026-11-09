package org.elis.retemonitor.modello;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "utenti")
public class Utente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** Password cifrata con BCrypt: quella in chiaro non viene mai salvata. */
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "creato_il", nullable = false)
    private LocalDateTime creatoIl;

    protected Utente() {
    }

    public Utente(String username, String passwordHash, LocalDateTime creatoIl) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.creatoIl = creatoIl;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public LocalDateTime getCreatoIl() { return creatoIl; }
}
