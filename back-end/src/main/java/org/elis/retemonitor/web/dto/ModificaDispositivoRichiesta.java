package org.elis.retemonitor.web.dto;

import org.elis.retemonitor.modello.CategoriaDispositivo;

import jakarta.validation.constraints.Size;

/**
 * Modifica parziale di un dispositivo: i campi null restano invariati.
 * Il nome vuoto ("") riporta al nome automatico.
 */
public record ModificaDispositivoRichiesta(
        @Size(max = 100, message = "Il nome può avere al massimo 100 caratteri") String nome,
        CategoriaDispositivo categoria,
        Boolean fidato) {
}
