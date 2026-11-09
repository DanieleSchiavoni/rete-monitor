package org.elis.retemonitor.repository;

import java.util.Optional;

import org.elis.retemonitor.modello.Utente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtenteRepository extends JpaRepository<Utente, Long> {

    Optional<Utente> findByUsername(String username);
}
