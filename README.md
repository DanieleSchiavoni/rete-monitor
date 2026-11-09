# Rete Monitor

Applicazione web che scansiona la rete Wi-Fi di casa, trova i dispositivi collegati e mostra quali sono accesi e quali spenti, con lo storico delle accensioni.

- **Backend:** Java 21+, Spring Boot 3.5, Spring Security + JWT, Spring Data JPA, Flyway, MySQL
- **Frontend:** Angular 19 (componenti standalone e signals), CSS puro con tema chiaro e scuro automatico

## Funzionalità

- Scansione automatica della rete ogni minuto, o su richiesta
- Stato acceso/spento di ogni dispositivo e storico delle accensioni
- **Notifiche** quando compare un dispositivo mai visto (campanella con contatore)
- **Dispositivi fidati e categorie**: segni quelli che conosci e scegli il tipo (smartphone, TV, stampante…), così quelli sconosciuti saltano all'occhio
- **Porte e servizi aperti**: controlla 27 porte comuni per capire che cos'è un dispositivo (stampante, telecamera, NAS…)
- **Accesso protetto** con Spring Security e JWT: al primo avvio si crea l'account, poi la registrazione si chiude

## Come funziona

1. Il backend individua la scheda di rete (Wi-Fi o cavo) e la sottorete, per esempio `192.168.1.0/24`.
2. Ogni minuto prova tutti i 254 indirizzi in parallelo: prima con il **ping**, poi, se non risponde, con alcune **porte TCP** comuni (80, 443, 22, 445, 62078 per iPhone…).
3. Legge la **tabella ARP** del sistema per ricavare il MAC address di ogni dispositivo. La tabella si riempie proprio grazie ai tentativi del punto 2, quindi trova anche dispositivi che non rispondono al ping.
4. Salva i dispositivi in MySQL usando il **MAC come chiave**: così un dispositivo viene riconosciuto anche se cambia IP.
5. Un dispositivo che non risponde per 2 scansioni consecutive diventa **spento** e viene registrato un evento nello storico.

```
back-end/src/main/java/org/elis/retemonitor
├── scansione/    logica di rete, senza Spring (ping, porte, ARP, sottorete, produttori)
├── sicurezza/    creazione e verifica dei token JWT, filtro delle richieste
├── config/       creazione dei bean, Spring Security e CORS
├── modello/      entità JPA: Dispositivo, EventoStato, Scansione, Notifica, PortaAperta, Utente
├── repository/   Spring Data JPA
├── servizio/     dispositivi, notifiche, porte, autenticazione, scansioni programmate
└── web/          controller REST, DTO e gestione degli errori
```

## Avvio

### 1. Backend

Requisiti: Java 21 o superiore, MySQL in esecuzione.

Il progetto non include il Maven Wrapper: copia `mvnw`, `mvnw.cmd` e la cartella `.mvn` da un altro tuo progetto Spring Boot dentro `back-end/` (oppure apri la cartella con IntelliJ, che usa il suo Maven).

```bash
cd back-end
export DB_PASSWORD=la_tua_password_mysql
export JWT_SEGRETO="$(openssl rand -base64 48)"
./mvnw spring-boot:run
```

Il database `rete_monitor` viene creato da solo e Flyway applica le migrazioni. Il backend parte sulla porta **8081** e la prima scansione inizia dopo 3 secondi.

`JWT_SEGRETO` è la chiave che firma i token (almeno 32 caratteri). Se manca ne viene generata una a ogni avvio: funziona, ma dopo ogni riavvio del backend bisogna rifare il login. Per tenerla fissa, salvala una volta e riusala.

### 2. Frontend

```bash
cd front-end
npm install
ng serve
```

Apri http://localhost:4200. Al primo accesso l'app chiede di creare l'account (username e password di almeno 8 caratteri); dalla volta dopo mostra il login.

Alla prima scansione tutti i dispositivi risultano "da verificare": riceverai una sola notifica di riepilogo. Segna come fidati quelli che riconosci (uno per uno dal dettaglio, oppure tutti insieme con il pulsante nell'avviso): da quel momento ogni dispositivo nuovo genera una notifica.

### 3. Produttori dei dispositivi (facoltativo)

Per vedere il produttore (Apple, Samsung, TP-Link…) scarica l'elenco ufficiale dell'IEEE e mettilo in `back-end/oui.csv`:

https://standards-oui.ieee.org/oui/oui.csv

Senza il file l'app funziona lo stesso, il produttore resta vuoto.

## API REST

Tutte le API richiedono l'intestazione `Authorization: Bearer <token>`, tranne le tre segnate come pubbliche.

| Metodo | Percorso | Descrizione |
|---|---|---|
| GET | `/api/auth/stato` | Pubblica. `{ "configurato": true/false }`: esiste già un account? |
| POST | `/api/auth/configura` | Pubblica, solo al primo avvio. Crea l'account e restituisce il token |
| POST | `/api/auth/login` | Pubblica. `{ "username", "password" }` → token JWT |
| GET | `/api/auth/io` | Utente collegato |
| GET | `/api/dispositivi` | Elenco dei dispositivi conosciuti (prima gli accesi) |
| GET | `/api/dispositivi/{id}` | Dettaglio di un dispositivo |
| GET | `/api/dispositivi/{id}/eventi` | Ultimi 100 eventi acceso/spento |
| PATCH | `/api/dispositivi/{id}` | Modifica parziale: `{ "nome": "TV salotto", "categoria": "TV", "fidato": true }` |
| POST | `/api/dispositivi/fidati` | Segna tutti i dispositivi come fidati |
| DELETE | `/api/dispositivi/{id}` | Elimina dispositivo, porte e storico |
| GET | `/api/dispositivi/{id}/porte` | Porte aperte trovate nell'ultimo controllo |
| POST | `/api/dispositivi/{id}/porte` | Controlla adesso le porte (qualche secondo) |
| GET | `/api/notifiche?soloNonLette=true` | Ultime 50 notifiche |
| GET | `/api/notifiche/conteggio` | Numero di notifiche non lette |
| POST | `/api/notifiche/{id}/letta` | Segna una notifica come letta |
| POST | `/api/notifiche/lette` | Segna tutte come lette |
| POST | `/api/scansioni` | Avvia subito una scansione (202, oppure 409 se già in corso) |
| GET | `/api/scansioni/stato` | Scansione in corso? Com'è andata l'ultima? |
| GET | `/api/rete` | Interfaccia, IP e sottorete usati |

## Configurazione

In `back-end/src/main/resources/application.properties`:

| Proprietà | Predefinito | Significato |
|---|---|---|
| `rete.intervallo-ms` | 60000 | Ogni quanto ripetere la scansione |
| `rete.interfaccia` | vuoto | Forza una scheda (es. `en0`) |
| `rete.sottorete` | vuoto | Forza una sottorete (es. `192.168.1.0/24`) |
| `rete.porte` | 80,443,22,… | Porte TCP provate dopo il ping |
| `rete.arp-come-presenza` | true | Conta come acceso chi è nella tabella ARP |
| `rete.scansioni-mancate-per-offline` | 2 | Scansioni senza risposta prima di "spento" |
| `rete.timeout-porte-dettaglio-ms` | 700 | Attesa per ogni porta nel controllo dettagliato |
| `jwt.segreto` | variabile `JWT_SEGRETO` | Chiave di firma dei token (almeno 32 caratteri) |
| `jwt.durata-ore` | 12 | Dopo quante ore il token scade e serve un nuovo login |

## Limiti da conoscere

- **Spento o irraggiungibile:** dalla rete non si può distinguere un dispositivo spento da uno che non risponde. Telefoni in standby e PC con firewall a volte non rispondono a nulla e risultano spenti.
- **Ritardo nello "spento":** il sistema operativo tiene i MAC nella tabella ARP per alcuni minuti, quindi un dispositivo appena spento può risultare acceso ancora per un po'. Con `rete.arp-come-presenza=false` lo stato è più reattivo ma si perdono i dispositivi che non rispondono a ping e porte.
- **MAC privati:** iPhone, Android e Windows recenti usano un MAC casuale per ogni rete. L'app li segnala come "MAC privato" e, se il telefono cambia MAC, compare come dispositivo nuovo.
- **macOS:** se la scansione non trova nessun dispositivo avviando il backend da IntelliJ o VS Code, controlla *Impostazioni di Sistema → Privacy e sicurezza → Rete locale* e abilita l'app. Da Terminale di solito non serve.
- **Sottoreti grandi:** per non rallentare, la scansione si limita sempre a una /24 (254 indirizzi).
- Usala solo su reti tue o dove hai il permesso.

## Test

```bash
cd back-end
./mvnw test
```

I test coprono la lettura della tabella ARP (formati macOS, Linux e Windows), il calcolo della sottorete, il riconoscimento dei MAC casuali, la lettura del file dei produttori e i token JWT (validi, alterati, scaduti, chiave troppo corta).

## Sicurezza

- Le password sono salvate cifrate con **BCrypt**, mai in chiaro.
- Il backend non tiene sessioni: ogni richiesta porta il proprio token JWT firmato, che scade dopo 12 ore.
- La registrazione è aperta solo finché non esiste nessun account.
- Il login risponde nello stesso tempo sia con username inesistente sia con password sbagliata, così non rivela quali username esistono.
