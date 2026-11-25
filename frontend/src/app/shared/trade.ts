import { Injectable, signal } from '@angular/core';
import type { OrderSide } from '../core/portfolio';

export interface TradeRequest {
  readonly ticker: string;
  readonly side: OrderSide;
}

/** Abre a gaveta de negociação de qualquer tela: linha da tabela, página do ativo, Radar. */
@Injectable({ providedIn: 'root' })
export class TradeService {
  readonly current = signal<TradeRequest | null>(null);

  open(ticker: string, side: OrderSide = 'BUY'): void {
    this.current.set({ ticker, side });
  }

  close(): void {
    this.current.set(null);
  }
}
