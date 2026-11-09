import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, map, tap } from 'rxjs';
import { URL_API } from '../config';
import { RispostaAccesso } from '../modelli/modelli';

const CHIAVE_SESSIONE = 'rete-monitor-sessione';

interface Sessione {
  token: string;
  username: string;
  scadenza: string;
}

/** Tiene il token JWT in memoria e nel localStorage, così il login sopravvive al ricaricamento della pagina. */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly sessione = signal<Sessione | null>(leggiSessione());

  readonly autenticato = computed(() => this.sessione() !== null);
  readonly username = computed(() => this.sessione()?.username ?? null);

  token(): string | null {
    const s = this.sessione();
    if (!s) return null;
    if (new Date(s.scadenza).getTime() <= Date.now()) {
      this.cancellaSessione();
      return null;
    }
    return s.token;
  }

  /** true se l'account esiste già (mostra "Accedi"), false al primo avvio (mostra "Crea l'account"). */
  configurato(): Observable<boolean> {
    return this.http
      .get<{ configurato: boolean }>(`${URL_API}/auth/stato`)
      .pipe(map((risposta) => risposta.configurato));
  }

  accedi(username: string, password: string): Observable<RispostaAccesso> {
    return this.http
      .post<RispostaAccesso>(`${URL_API}/auth/login`, { username, password })
      .pipe(tap((risposta) => this.salva(risposta)));
  }

  creaAccount(username: string, password: string): Observable<RispostaAccesso> {
    return this.http
      .post<RispostaAccesso>(`${URL_API}/auth/configura`, { username, password })
      .pipe(tap((risposta) => this.salva(risposta)));
  }

  esci(): void {
    this.cancellaSessione();
    this.router.navigate(['/accesso']);
  }

  private cancellaSessione(): void {
    this.sessione.set(null);
    try {
      localStorage.removeItem(CHIAVE_SESSIONE);
    } catch {
      // localStorage non disponibile: la sessione era solo in memoria
    }
  }

  private salva(risposta: RispostaAccesso): void {
    const sessione: Sessione = { token: risposta.token, username: risposta.username, scadenza: risposta.scadenza };
    this.sessione.set(sessione);
    try {
      localStorage.setItem(CHIAVE_SESSIONE, JSON.stringify(sessione));
    } catch {
      // localStorage non disponibile: la sessione resta solo in memoria
    }
  }
}

function leggiSessione(): Sessione | null {
  try {
    const testo = localStorage.getItem(CHIAVE_SESSIONE);
    if (!testo) return null;
    const sessione = JSON.parse(testo) as Sessione;
    return new Date(sessione.scadenza).getTime() > Date.now() ? sessione : null;
  } catch {
    return null;
  }
}
