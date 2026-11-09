import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../servizi/auth.service';

@Component({
  selector: 'app-accesso',
  imports: [FormsModule],
  templateUrl: './accesso.component.html',
})
export class AccessoComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  /** null = in caricamento, false = primo avvio (creazione account), true = login normale. */
  readonly configurato = signal<boolean | null>(null);
  readonly errore = signal<string | null>(null);
  readonly invioInCorso = signal(false);

  username = '';
  password = '';
  confermaPassword = '';

  ngOnInit(): void {
    this.auth.configurato().subscribe({
      next: (configurato) => this.configurato.set(configurato),
      error: () => this.errore.set('Il backend non risponde. È avviato sulla porta 8081?'),
    });
  }

  invia(): void {
    this.errore.set(null);
    if (this.configurato() === false && this.password !== this.confermaPassword) {
      this.errore.set('Le due password non coincidono');
      return;
    }

    this.invioInCorso.set(true);
    const richiesta = this.configurato()
      ? this.auth.accedi(this.username, this.password)
      : this.auth.creaAccount(this.username, this.password);

    richiesta.subscribe({
      next: () => this.router.navigate(['/']),
      error: (errore: HttpErrorResponse) => {
        this.invioInCorso.set(false);
        this.errore.set(errore.error?.messaggio ?? 'Accesso non riuscito');
      },
    });
  }
}
