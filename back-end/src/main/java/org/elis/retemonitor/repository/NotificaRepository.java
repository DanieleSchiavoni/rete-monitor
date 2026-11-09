package org.elis.retemonitor.repository;

import java.util.List;

import org.elis.retemonitor.modello.Notifica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificaRepository extends JpaRepository<Notifica, Long> {

    List<Notifica> findTop50ByOrderByCreataIlDesc();

    List<Notifica> findTop50ByLettaFalseOrderByCreataIlDesc();

    long countByLettaFalse();

    @Modifying
    @Query("update Notifica n set n.letta = true where n.letta = false")
    int segnaTutteLette();

    @Modifying
    @Query("update Notifica n set n.letta = true where n.letta = false and n.dispositivo.id = :dispositivoId")
    int segnaLetteDelDispositivo(@Param("dispositivoId") Long dispositivoId);
}
