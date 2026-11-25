import { Injectable, signal } from '@angular/core';

const KEY = 'simulador.radar';

/** O Radar: ativos que o usuário quer acompanhar, guardados no navegador até existir login. */
@Injectable({ providedIn: 'root' })
export class WatchlistService {
  readonly tickers = signal<readonly string[]>(read());

  has(ticker: string): boolean {
    return this.tickers().includes(ticker);
  }

  toggle(ticker: string): void {
    this.tickers.update((list) =>
      list.includes(ticker) ? list.filter((t) => t !== ticker) : [...list, ticker],
    );
    write(this.tickers());
  }
}

function read(): readonly string[] {
  try {
    const raw = globalThis.localStorage?.getItem(KEY);
    const parsed: unknown = raw ? JSON.parse(raw) : [];
    return Array.isArray(parsed) ? parsed.filter((t): t is string => typeof t === 'string') : [];
  } catch {
    return [];
  }
}

function write(list: readonly string[]): void {
  try {
    globalThis.localStorage?.setItem(KEY, JSON.stringify(list));
  } catch {
    // Sem armazenamento, o Radar vale só nesta aba.
  }
}
