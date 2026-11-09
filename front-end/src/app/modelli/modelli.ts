export type Categoria =
  | 'SCONOSCIUTO'
  | 'SMARTPHONE'
  | 'TABLET'
  | 'COMPUTER'
  | 'TV'
  | 'CONSOLE'
  | 'STAMPANTE'
  | 'ROUTER'
  | 'SMART_HOME'
  | 'TELECAMERA'
  | 'NAS'
  | 'ALTRO';

export interface Dispositivo {
  id: number;
  nome: string;
  nomePersonalizzato: string | null;
  nomeHost: string | null;
  indirizzoIp: string;
  indirizzoMac: string | null;
  produttore: string | null;
  macCasuale: boolean;
  questoComputer: boolean;
  online: boolean;
  fidato: boolean;
  categoria: Categoria;
  metodoRilevamento: string | null;
  primaVolta: string;
  ultimaVolta: string;
  porteControllateIl: string | null;
}

export interface ModificaDispositivo {
  nome?: string;
  categoria?: Categoria;
  fidato?: boolean;
}

export interface EventoStato {
  id: number;
  stato: 'ONLINE' | 'OFFLINE';
  istante: string;
}

export interface Porta {
  porta: number;
  servizio: string;
}

export interface Notifica {
  id: number;
  tipo: 'NUOVO_DISPOSITIVO' | 'PRIMA_SCANSIONE';
  messaggio: string;
  letta: boolean;
  creataIl: string;
  dispositivoId: number | null;
}

export interface StatoScansione {
  inCorso: boolean;
  inizio: string | null;
  fine: string | null;
  sottorete: string | null;
  indirizziControllati: number | null;
  dispositiviTrovati: number | null;
  errore: string | null;
}

export interface InfoRete {
  interfaccia: string | null;
  indirizzoIp: string | null;
  indirizzoMac: string | null;
  sottorete: string | null;
  errore: string | null;
}

export interface RispostaAccesso {
  token: string;
  username: string;
  scadenza: string;
}

export type Filtro = 'tutti' | 'online' | 'offline' | 'daVerificare';
