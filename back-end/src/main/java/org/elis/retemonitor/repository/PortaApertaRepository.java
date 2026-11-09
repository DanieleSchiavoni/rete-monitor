package org.elis.retemonitor.repository;

import java.util.List;

import org.elis.retemonitor.modello.PortaAperta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PortaApertaRepository extends JpaRepository<PortaAperta, Long> {

    List<PortaAperta> findByDispositivoIdOrderByPortaAsc(Long dispositivoId);

    @Modifying
    @Query("delete from PortaAperta p where p.dispositivo.id = :dispositivoId")
    int eliminaDelDispositivo(@Param("dispositivoId") Long dispositivoId);
}
