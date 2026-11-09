package org.elis.retemonitor.repository;

import java.util.Optional;

import org.elis.retemonitor.modello.Scansione;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScansioneRepository extends JpaRepository<Scansione, Long> {

    Optional<Scansione> findTopByOrderByInizioDesc();
}
