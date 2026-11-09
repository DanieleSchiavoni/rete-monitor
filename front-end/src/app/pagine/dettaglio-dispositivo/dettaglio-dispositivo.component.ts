import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Categoria, Dispositivo, EventoStato, Porta } from '../../modelli/modelli';
import { NotificheStato } from '../../servizi/notifiche-stato.service';
import { ReteService } from '../../servizi/rete.service';
import { CATEGORIE, infoCategoria } from '../../utilita/categorie';
import { tempoTrascorso } from '../../utilita/tempo';

@Component({
  selector: 'app-dettaglio-dispositivo',
  imports: [RouterLink, FormsModule, DatePipe],
  templateUrl: './dettaglio-dispositivo.component.html',
})
export class DettaglioDispositivoComponent implements OnInit {
  private readonly servizio = inject(ReteService);
  private readonly notifiche = inject(NotificheStato);
  private readonly router = inject(Router);

  /** Parametro :id dell'indirizzo, passato automaticamente dal router. */
  readonly id = input.required<string>();

  readonly dispositivo = signal<Dispositivo | null>(null);
  readonly eventi = signal<EventoStato[]>([]);
  readonly porte = signal<Porta[]>([]);
  readonly controlloPorteInCorso = signal(false);
  readonly errore = signal<string | null>(null);
  readonly messaggio = signal<string | null>(null);

  readonly categorie = CATEGORIE;
  readonly tempoTrascorso = tempoTrascorso;

  // Campi del modulo "Impostazioni"
  nuovoNome = '';
  nuovaCategoria: Categoria = 'SCONOSCIUTO';
  nuovoFidato = false;

  ngOnInit(): void {
    const id = Number(this.id());
    forkJoin({
      dispositivo: this.servizio.dispositivo(id),
      eventi: this.servizio.eventi(id),
      porte: this.servizio.porte(id),
    }).subscribe({
      next: ({ dispositivo, eventi, porte }) => {
        this.impostaDispositivo(dispositivo);
        this.eventi.set(eventi);
        this.porte.set(porte);
      },
      error: () => this.errore.set('Dispositivo non trovato o backend non raggiungibile.'),
    });
  }

  icona(categoria: Categoria): string {
    return infoCategoria(categoria).icona;
  }

  salva(): void {
    const d = this.dispositivo();
    if (!d) return;
    this.messaggio.set(null);
    this.servizio
      .modifica(d.id, { nome: this.nuovoNome, categoria: this.nuovaCategoria, fidato: this.nuovoFidato })
      .subscribe({
        next: (aggiornato) => {
          this.impostaDispositivo(aggiornato);
          this.messaggio.set('Modifiche salvate');
          this.notifiche.aggiorna();
        },
        error: () => this.messaggio.set('Non è stato possibile salvare le modifiche'),
      });
  }

  controllaPorte(): void {
    const d = this.dispositivo();
    if (!d) return;
    this.controlloPorteInCorso.set(true);
    this.servizio.controllaPorte(d.id).subscribe({
      next: (porte) => {
        this.porte.set(porte);
        this.dispositivo.set({ ...d, porteControllateIl: new Date().toISOString() });
        this.controlloPorteInCorso.set(false);
      },
      error: () => this.controlloPorteInCorso.set(false),
    });
  }

  elimina(): void {
    const d = this.dispositivo();
    if (!d) return;
    const conferma = confirm(
      `Eliminare "${d.nome}" e il suo storico? Se è ancora acceso ricomparirà alla prossima scansione.`,
    );
    if (!conferma) return;
    this.servizio.elimina(d.id).subscribe(() => {
      this.notifiche.aggiorna();
      this.router.navigate(['/']);
    });
  }

  private impostaDispositivo(d: Dispositivo): void {
    this.dispositivo.set(d);
    this.nuovoNome = d.nomePersonalizzato ?? '';
    this.nuovaCategoria = d.categoria;
    this.nuovoFidato = d.fidato;
  }
}
