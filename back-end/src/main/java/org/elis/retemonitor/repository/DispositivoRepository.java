package org.elis.retemonitor.repository;

import java.util.List;
import java.util.Optional;

import org.elis.retemonitor.modello.Dispositivo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DispositivoRepository extends JpaRepository<Dispositivo, Long> {

    Optional<Dispositivo> findByChiave(String chiave);

    List<Dispositivo> findAllByOrderByOnlineDescUltimaVoltaDesc();
}
