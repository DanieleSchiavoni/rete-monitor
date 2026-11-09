import { Categoria } from '../modelli/modelli';

export interface InfoCategoria {
  valore: Categoria;
  etichetta: string;
  icona: string;
}

export const CATEGORIE: InfoCategoria[] = [
  { valore: 'SCONOSCIUTO', etichetta: 'Non specificato', icona: '❔' },
  { valore: 'SMARTPHONE', etichetta: 'Smartphone', icona: '📱' },
  { valore: 'TABLET', etichetta: 'Tablet', icona: '📲' },
  { valore: 'COMPUTER', etichetta: 'Computer', icona: '💻' },
  { valore: 'TV', etichetta: 'TV / media', icona: '📺' },
  { valore: 'CONSOLE', etichetta: 'Console', icona: '🎮' },
  { valore: 'STAMPANTE', etichetta: 'Stampante', icona: '🖨️' },
  { valore: 'ROUTER', etichetta: 'Router / rete', icona: '📡' },
  { valore: 'SMART_HOME', etichetta: 'Smart home', icona: '💡' },
  { valore: 'TELECAMERA', etichetta: 'Telecamera', icona: '📷' },
  { valore: 'NAS', etichetta: 'NAS / server', icona: '🗄️' },
  { valore: 'ALTRO', etichetta: 'Altro', icona: '🔌' },
];

export function infoCategoria(valore: Categoria): InfoCategoria {
  return CATEGORIE.find((c) => c.valore === valore) ?? CATEGORIE[0];
}
