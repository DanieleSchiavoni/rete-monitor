import { Routes } from '@angular/router';
import { autenticatoGuard, ospiteGuard } from './servizi/auth.guard';
import { AccessoComponent } from './pagine/accesso/accesso.component';
import { ElencoDispositiviComponent } from './pagine/elenco-dispositivi/elenco-dispositivi.component';
import { DettaglioDispositivoComponent } from './pagine/dettaglio-dispositivo/dettaglio-dispositivo.component';
import { NotificheComponent } from './pagine/notifiche/notifiche.component';

export const routes: Routes = [
  { path: 'accesso', component: AccessoComponent, canActivate: [ospiteGuard], title: 'Accesso · Rete Monitor' },
  { path: '', component: ElencoDispositiviComponent, canActivate: [autenticatoGuard], title: 'Dispositivi · Rete Monitor' },
  {
    path: 'dispositivi/:id',
    component: DettaglioDispositivoComponent,
    canActivate: [autenticatoGuard],
    title: 'Dettaglio · Rete Monitor',
  },
  { path: 'notifiche', component: NotificheComponent, canActivate: [autenticatoGuard], title: 'Notifiche · Rete Monitor' },
  { path: '**', redirectTo: '' },
];
