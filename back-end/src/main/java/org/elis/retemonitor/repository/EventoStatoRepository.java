package org.elis.retemonitor.repository;

import java.util.List;

import org.elis.retemonitor.modello.EventoStato;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoStatoRepository extends JpaRepository<EventoStato, Long> {

    List<EventoStato> findTop100ByDispositivoIdOrderByIstanteDesc(Long dispositivoId);
}
