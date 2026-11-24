import { DestroyRef, Injectable, inject, signal } from '@angular/core';
import { Client } from '@stomp/stompjs';
import { Quote } from './quote';

export type ConnectionState = 'connecting' | 'connected' | 'disconnected';

/** Intervalo de agrupamento: a tela redesenha no máximo 10 vezes por segundo. */
const BATCH_MS = 100;

/**
 * Assina /topic/quotes no backend por STOMP e reconecta sozinho se a conexão cair. Entrega as
 * cotações em lotes, porque na abertura do pregão chegam centenas por segundo.
 */
@Injectable({ providedIn: 'root' })
export class QuoteStream {
  private client: Client | null = null;
  readonly state = signal<ConnectionState>('disconnected');

  constructor() {
    inject(DestroyRef).onDestroy(() => this.disconnect());
  }

  connect(onBatch: (quotes: Quote[]) => void): void {
    if (this.client) {
      return;
    }
    const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
    const client = new Client({
      brokerURL: `${protocol}://${location.host}/ws`,
      reconnectDelay: 3000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
    });
    let pending: Quote[] = [];
    let timer: ReturnType<typeof setTimeout> | null = null;
    const flush = (): void => {
      timer = null;
      const batch = pending;
      pending = [];
      onBatch(batch);
    };
    client.onConnect = () => {
      this.state.set('connected');
      client.subscribe('/topic/quotes', (message) => {
        pending.push(JSON.parse(message.body) as Quote);
        timer ??= setTimeout(flush, BATCH_MS);
      });
    };
    client.onWebSocketClose = () => this.state.set('connecting');
    this.state.set('connecting');
    client.activate();
    this.client = client;
  }

  disconnect(): void {
    void this.client?.deactivate();
    this.client = null;
    this.state.set('disconnected');
  }
}
