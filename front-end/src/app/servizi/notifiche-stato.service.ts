import { Injectable, inject, signal } from '@angular/core';
import { catchError, of } from 'rxjs';
import { ReteService } from './rete.service';

/** Conteggio delle notifiche non lette, condiviso tra la campanella dell'intestazione e le pagine. */
@Injectable({ providedIn: 'root' })
export class NotificheStato {
  private readonly servizio = inject(ReteService);

  readonly nonLette = signal(0);

  aggiorna(): void {
    this.servizio
      .conteggioNotifiche()
      .pipe(catchError(() => of(this.nonLette())))
      .subscribe((numero) => this.nonLette.set(numero));
  }
}
