import { Component, effect, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink, RouterOutlet } from '@angular/router';
import { timer } from 'rxjs';
import { InfoRete } from './modelli/modelli';
import { AuthService } from './servizi/auth.service';
import { NotificheStato } from './servizi/notifiche-stato.service';
import { ReteService } from './servizi/rete.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink],
  template: `
    <header class="intestazione">
      <a routerLink="/" class="marchio">
        <span class="marchio-icona" aria-hidden="true">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <path d="M2 8.5a15 15 0 0 1 20 0" /><path d="M5 12a10 10 0 0 1 14 0" /><path d="M8.5 15.5a5 5 0 0 1 7 0" />
            <circle cx="12" cy="19" r="1.2" fill="currentColor" />
          </svg>
        </span>
        Rete Monitor
      </a>

      @if (auth.autenticato()) {
        <div class="intestazione-destra">
          @if (rete(); as r) {
            @if (r.errore) {
              <span class="info-rete info-rete-errore">{{ r.errore }}</span>
            } @else {
              <span class="info-rete">{{ r.interfaccia }} · {{ r.indirizzoIp }} · {{ r.sottorete }}</span>
            }
          }
          <a routerLink="/notifiche" class="campanella" aria-label="Notifiche">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9" /><path d="M10.3 21a1.94 1.94 0 0 0 3.4 0" />
            </svg>
            @if (notifiche.nonLette() > 0) {
              <span class="contatore">{{ notifiche.nonLette() > 99 ? '99+' : notifiche.nonLette() }}</span>
            }
          </a>
          <span class="utente">{{ auth.username() }}</span>
          <button type="button" class="pulsante pulsante-secondario pulsante-piccolo" (click)="auth.esci()">Esci</button>
        </div>
      }
    </header>
    <main class="contenuto">
      <router-outlet />
    </main>
  `,
})
export class AppComponent {
  readonly auth = inject(AuthService);
  readonly notifiche = inject(NotificheStato);
  private readonly servizio = inject(ReteService);

  readonly rete = signal<InfoRete | null>(null);

  constructor() {
    // Dopo l'accesso carica le informazioni sulla rete
    effect(() => {
      if (this.auth.autenticato()) {
        this.servizio.infoRete().subscribe({
          next: (info) => this.rete.set(info),
          error: () => this.rete.set(null),
        });
      } else {
        this.rete.set(null);
      }
    });

    // Controlla le notifiche nuove ogni 10 secondi
    timer(0, 10000)
      .pipe(takeUntilDestroyed())
      .subscribe(() => {
        if (this.auth.token()) this.notifiche.aggiorna();
      });
  }
}
