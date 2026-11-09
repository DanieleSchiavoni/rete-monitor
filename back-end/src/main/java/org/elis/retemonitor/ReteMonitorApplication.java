package org.elis.retemonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * L'utente predefinito di Spring Security (con password stampata nei log) è disattivato:
 * gli utenti sono nella tabella "utenti" e l'accesso avviene con JWT.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableScheduling
public class ReteMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReteMonitorApplication.class, args);
    }
}
