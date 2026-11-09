package org.elis.retemonitor.config;

import java.time.Duration;

import org.elis.retemonitor.sicurezza.FiltroJwt;
import org.elis.retemonitor.sicurezza.ServizioJwt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class ConfigurazioneSicurezza {

    @Bean
    public ServizioJwt servizioJwt(@Value("${jwt.segreto:}") String segreto,
                                   @Value("${jwt.durata-ore:12}") long durataOre) {
        return new ServizioJwt(segreto, Duration.ofHours(durataOre));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain catenaSicurezza(HttpSecurity http, ServizioJwt servizioJwt) throws Exception {
        http
                // API senza sessione: il token viaggia in ogni richiesta, quindi CSRF non serve
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sessione -> sessione.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .exceptionHandling(errori -> errori
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(regole -> regole
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/stato", "/api/auth/login", "/api/auth/configura").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new FiltroJwt(servizioJwt), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
