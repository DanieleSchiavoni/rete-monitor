package org.elis.retemonitor.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/** Permette al frontend Angular (ng serve sulla 4200) di chiamare le API, token compreso. */
@Configuration
public class ConfigurazioneCors {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configurazione = new CorsConfiguration();
        configurazione.setAllowedOrigins(List.of("http://localhost:4200"));
        configurazione.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        configurazione.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource sorgente = new UrlBasedCorsConfigurationSource();
        sorgente.registerCorsConfiguration("/api/**", configurazione);
        return sorgente;
    }
}
