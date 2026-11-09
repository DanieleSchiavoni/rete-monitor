/** Trasforma una data ISO in un testo come "adesso", "5 min fa", "3 ore fa", "2 giorni fa". */
export function tempoTrascorso(dataIso: string | null, adesso: number = Date.now()): string {
  if (!dataIso) return '—';
  const secondi = Math.max(0, Math.round((adesso - new Date(dataIso).getTime()) / 1000));
  if (secondi < 60) return 'adesso';
  const minuti = Math.floor(secondi / 60);
  if (minuti < 60) return `${minuti} min fa`;
  const ore = Math.floor(minuti / 60);
  if (ore < 24) return ore === 1 ? '1 ora fa' : `${ore} ore fa`;
  const giorni = Math.floor(ore / 24);
  return giorni === 1 ? 'ieri' : `${giorni} giorni fa`;
}
