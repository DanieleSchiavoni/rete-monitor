import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { URL_API } from '../config';
import {
  Dispositivo,
  EventoStato,
  InfoRete,
  ModificaDispositivo,
  Notifica,
  Porta,
  StatoScansione,
} from '../modelli/modelli';

@Injectable({ providedIn: 'root' })
export class ReteService {
  private readonly http = inject(HttpClient);

  // ---------- Dispositivi ----------

  elencoDispositivi(): Observable<Dispositivo[]> {
    return this.http.get<Dispositivo[]>(`${URL_API}/dispositivi`);
  }

  dispositivo(id: number): Observable<Dispositivo> {
    return this.http.get<Dispositivo>(`${URL_API}/dispositivi/${id}`);
  }

  eventi(id: number): Observable<EventoStato[]> {
    return this.http.get<EventoStato[]>(`${URL_API}/dispositivi/${id}/eventi`);
  }

  modifica(id: number, modifica: ModificaDispositivo): Observable<Dispositivo> {
    return this.http.patch<Dispositivo>(`${URL_API}/dispositivi/${id}`, modifica);
  }

  segnaTuttiFidati(): Observable<number> {
    return this.http
      .post<{ modificati: number }>(`${URL_API}/dispositivi/fidati`, null)
      .pipe(map((r) => r.modificati));
  }

  elimina(id: number): Observable<void> {
    return this.http.delete<void>(`${URL_API}/dispositivi/${id}`);
  }

  // ---------- Porte ----------

  porte(id: number): Observable<Porta[]> {
    return this.http.get<Porta[]>(`${URL_API}/dispositivi/${id}/porte`);
  }

  controllaPorte(id: number): Observable<Porta[]> {
    return this.http.post<Porta[]>(`${URL_API}/dispositivi/${id}/porte`, null);
  }

  // ---------- Notifiche ----------

  notifiche(soloNonLette = false): Observable<Notifica[]> {
    const parametri = new HttpParams().set('soloNonLette', soloNonLette);
    return this.http.get<Notifica[]>(`${URL_API}/notifiche`, { params: parametri });
  }

  conteggioNotifiche(): Observable<number> {
    return this.http.get<{ nonLette: number }>(`${URL_API}/notifiche/conteggio`).pipe(map((r) => r.nonLette));
  }

  segnaNotificaLetta(id: number): Observable<void> {
    return this.http.post<void>(`${URL_API}/notifiche/${id}/letta`, null);
  }

  segnaTutteLette(): Observable<number> {
    return this.http.post<{ modificate: number }>(`${URL_API}/notifiche/lette`, null).pipe(map((r) => r.modificate));
  }

  // ---------- Scansioni e rete ----------

  statoScansione(): Observable<StatoScansione> {
    return this.http.get<StatoScansione>(`${URL_API}/scansioni/stato`);
  }

  avviaScansione(): Observable<StatoScansione> {
    return this.http.post<StatoScansione>(`${URL_API}/scansioni`, null);
  }

  infoRete(): Observable<InfoRete> {
    return this.http.get<InfoRete>(`${URL_API}/rete`);
  }
}
