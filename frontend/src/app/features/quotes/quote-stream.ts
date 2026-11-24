import { DestroyRef, Injectable, inject, signal } from '@angular/core';
import { Client } from '@stomp/stompjs';
import { Quote } from './quote';

export type ConnectionState = 'connecting' | 'connected' | 'disconnected';

/** Assina /topic/quotes no backend por STOMP e reconecta sozinho se a conexão cair. */
@Injectable({ providedIn: 'root' })
export class QuoteStream {
  private client: Client | null = null;
  readonly state = signal<ConnectionState>('disconnected');

  constructor() {
    inject(DestroyRef).onDestroy(() => this.disconnect());
  }

  connect(onQuote: (quote: Quote) => void): void {
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
    client.onConnect = () => {
      this.state.set('connected');
      client.subscribe('/topic/quotes', (message) => onQuote(JSON.parse(message.body) as Quote));
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
