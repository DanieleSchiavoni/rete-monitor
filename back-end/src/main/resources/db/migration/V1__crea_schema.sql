CREATE TABLE dispositivi (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    -- MAC address (o "ip:<indirizzo>" se il MAC non è noto): identifica il dispositivo tra una scansione e l'altra
    chiave              VARCHAR(64)  NOT NULL,
    indirizzo_mac       VARCHAR(17),
    indirizzo_ip        VARCHAR(15)  NOT NULL,
    nome_host           VARCHAR(255),
    nome_personalizzato VARCHAR(100),
    produttore          VARCHAR(255),
    mac_casuale         BOOLEAN      NOT NULL DEFAULT FALSE,
    questo_computer     BOOLEAN      NOT NULL DEFAULT FALSE,
    online              BOOLEAN      NOT NULL,
    scansioni_mancate   INT          NOT NULL DEFAULT 0,
    metodo_rilevamento  VARCHAR(20),
    prima_volta         DATETIME(6)  NOT NULL,
    ultima_volta        DATETIME(6)  NOT NULL,
    CONSTRAINT uk_dispositivi_chiave UNIQUE (chiave)
);

CREATE TABLE eventi_stato (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    dispositivo_id BIGINT      NOT NULL,
    stato          VARCHAR(10) NOT NULL,
    istante        DATETIME(6) NOT NULL,
    CONSTRAINT fk_eventi_dispositivo FOREIGN KEY (dispositivo_id)
        REFERENCES dispositivi (id) ON DELETE CASCADE,
    INDEX idx_eventi_dispositivo_istante (dispositivo_id, istante)
);

CREATE TABLE scansioni (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    inizio                DATETIME(6)  NOT NULL,
    fine                  DATETIME(6),
    sottorete             VARCHAR(18)  NOT NULL,
    indirizzi_controllati INT          NOT NULL,
    dispositivi_trovati   INT          NOT NULL,
    errore                VARCHAR(500)
);
