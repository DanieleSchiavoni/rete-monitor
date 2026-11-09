import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Notifica } from '../../modelli/modelli';
import { NotificheStato } from '../../servizi/notifiche-stato.service';
import { ReteService } from '../../servizi/rete.service';

@Component({
  selector: 'app-notifiche',
  imports: [DatePipe],
  templateUrl: './notifiche.component.html',
})
export class NotificheComponent implements OnInit {
  private readonly servizio = inject(ReteService);
  private readonly stato = inject(NotificheStato);
  private readonly router = inject(Router);

  readonly notifiche = signal<Notifica[]>([]);
  readonly caricato = signal(false);

  ngOnInit(): void {
    this.carica();
  }

  apri(notifica: Notifica): void {
    if (!notifica.letta) {
      this.servizio.segnaNotificaLetta(notifica.id).subscribe(() => this.stato.aggiorna());
      this.notifiche.update((elenco) => elenco.map((n) => (n.id === notifica.id ? { ...n, letta: true } : n)));
    }
    if (notifica.dispositivoId !== null) {
      this.router.navigate(['/dispositivi', notifica.dispositivoId]);
    }
  }

  segnaTutteLette(): void {
    this.servizio.segnaTutteLette().subscribe(() => {
      this.stato.aggiorna();
      this.carica();
    });
  }

  haNonLette(): boolean {
    return this.notifiche().some((n) => !n.letta);
  }

  private carica(): void {
    this.servizio.notifiche().subscribe((elenco) => {
      this.notifiche.set(elenco);
      this.caricato.set(true);
    });
  }
}
