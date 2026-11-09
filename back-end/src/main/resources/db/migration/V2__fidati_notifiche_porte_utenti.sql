-- Dispositivi fidati e categorie
ALTER TABLE dispositivi
    ADD COLUMN fidato               BOOLEAN     NOT NULL DEFAULT FALSE,
    ADD COLUMN categoria            VARCHAR(20) NOT NULL DEFAULT 'SCONOSCIUTO',
    ADD COLUMN porte_controllate_il DATETIME(6);

-- Notifiche (es. nuovo dispositivo in rete)
CREATE TABLE notifiche (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    dispositivo_id BIGINT,
    tipo           VARCHAR(30)  NOT NULL,
    messaggio      VARCHAR(300) NOT NULL,
    letta          BOOLEAN      NOT NULL DEFAULT FALSE,
    creata_il      DATETIME(6)  NOT NULL,
    CONSTRAINT fk_notifiche_dispositivo FOREIGN KEY (dispositivo_id)
        REFERENCES dispositivi (id) ON DELETE CASCADE,
    INDEX idx_notifiche_letta_data (letta, creata_il)
);

-- Porte aperte trovate nell'ultimo controllo di ogni dispositivo
CREATE TABLE porte_aperte (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    dispositivo_id BIGINT      NOT NULL,
    porta          INT         NOT NULL,
    servizio       VARCHAR(60) NOT NULL,
    CONSTRAINT fk_porte_dispositivo FOREIGN KEY (dispositivo_id)
        REFERENCES dispositivi (id) ON DELETE CASCADE
);

-- Utenti che possono accedere alla dashboard
CREATE TABLE utenti (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    creato_il     DATETIME(6)  NOT NULL,
    CONSTRAINT uk_utenti_username UNIQUE (username)
);
