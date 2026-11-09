import { Component, computed, signal, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, of, switchMap, timer } from 'rxjs';
import { INTERVALLO_AGGIORNAMENTO_MS } from '../../config';
import { Categoria, Dispositivo, Filtro, StatoScansione } from '../../modelli/modelli';
import { NotificheStato } from '../../servizi/notifiche-stato.service';
import { ReteService } from '../../servizi/rete.service';
import { infoCategoria } from '../../utilita/categorie';
import { tempoTrascorso } from '../../utilita/tempo';

@Component({
  selector: 'app-elenco-dispositivi',
  imports: [RouterLink],
  templateUrl: './elenco-dispositivi.component.html',
})
export class ElencoDispositiviComponent {
  private readonly servizio = inject(ReteService);
  private readonly notifiche = inject(NotificheStato);

  readonly dispositivi = signal<Dispositivo[]>([]);
  readonly stato = signal<StatoScansione | null>(null);
  readonly filtro = signal<Filtro>('tutti');
  readonly ricerca = signal('');
  readonly errore = signal<string | null>(null);
  readonly caricato = signal(false);
  readonly adesso = signal(Date.now());

  readonly totale = computed(() => this.dispositivi().length);
  readonly accesi = computed(() => this.dispositivi().filter((d) => d.online).length);
  readonly spenti = computed(() => this.totale() - this.accesi());
  readonly daVerificare = computed(() => this.dispositivi().filter((d) => !d.fidato).length);

  readonly filtrati = computed(() => {
    const testo = this.ricerca().trim().toLowerCase();
    return this.dispositivi().filter((d) => {
      if (this.filtro() === 'online' && !d.online) return false;
      if (this.filtro() === 'offline' && d.online) return false;
      if (this.filtro() === 'daVerificare' && d.fidato) return false;
      if (!testo) return true;
      return [d.nome, d.indirizzoIp, d.indirizzoMac, d.produttore, d.nomeHost, infoCategoria(d.categoria).etichetta]
        .some((campo) => campo?.toLowerCase().includes(testo));
    });
  });

  readonly tempoTrascorso = tempoTrascorso;

  constructor() {
    // Ricarica dispositivi e stato della scansione ogni pochi secondi finché la pagina è aperta
    timer(0, INTERVALLO_AGGIORNAMENTO_MS)
      .pipe(
        switchMap(() =>
          forkJoin({
            dispositivi: this.servizio.elencoDispositivi(),
            stato: this.servizio.statoScansione(),
          }).pipe(catchError(() => of(null))),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((risposta) => {
        this.adesso.set(Date.now());
        this.caricato.set(true);
        if (!risposta) {
          this.errore.set('Il backend non risponde. È avviato sulla porta 8081?');
          return;
        }
        this.errore.set(null);
        this.dispositivi.set(risposta.dispositivi);
        this.stato.set(risposta.stato);
      });
  }

  icona(categoria: Categoria): string {
    return infoCategoria(categoria).icona;
  }

  scansiona(): void {
    this.stato.update((s) => (s ? { ...s, inCorso: true } : s));
    this.servizio.avviaScansione().subscribe({
      next: (stato) => this.stato.set({ ...stato, inCorso: true }),
      // 409 = c'era già una scansione in corso: va bene così
      error: () => undefined,
    });
  }

  segnaTuttiFidati(): void {
    const conferma = confirm(
      `Segnare come fidati tutti i ${this.daVerificare()} dispositivi da verificare? ` +
        'Fallo solo se li riconosci tutti: da quel momento ti avviseremo solo per quelli nuovi.',
    );
    if (!conferma) return;
    this.servizio.segnaTuttiFidati().subscribe(() => {
      this.dispositivi.update((elenco) => elenco.map((d) => ({ ...d, fidato: true })));
      if (this.filtro() === 'daVerificare') this.filtro.set('tutti');
      this.notifiche.aggiorna();
    });
  }

  impostaRicerca(evento: Event): void {
    this.ricerca.set((evento.target as HTMLInputElement).value);
  }

  descriviMetodo(metodo: string | null): string {
    if (!metodo) return '';
    if (metodo === 'PING') return 'risponde al ping';
    if (metodo === 'ARP') return 'visto nella tabella ARP';
    if (metodo === 'LOCALE') return 'questo computer';
    if (metodo.startsWith('TCP:')) return `porta ${metodo.substring(4)} aperta`;
    return metodo;
  }
}
