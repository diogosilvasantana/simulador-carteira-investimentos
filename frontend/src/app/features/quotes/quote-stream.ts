import { DestroyRef, Injectable, inject, signal } from '@angular/core';
import { Client, StompSubscription } from '@stomp/stompjs';
import { Quote } from './quote';

export type ConnectionState = 'connecting' | 'connected' | 'disconnected';

interface Watch {
  readonly topic: string;
  readonly onBatch: (quotes: Quote[]) => void;
}

/**
 * Conexão STOMP com o backend. Assina um tópico por vez (a categoria aberta) e refaz a
 * assinatura sozinho quando a conexão volta. O backend já envia lotes a cada 500 ms (ADR-010).
 */
@Injectable({ providedIn: 'root' })
export class QuoteStream {
  private client: Client | null = null;
  private watch: Watch | null = null;
  private subscription: StompSubscription | null = null;
  readonly state = signal<ConnectionState>('disconnected');

  constructor() {
    inject(DestroyRef).onDestroy(() => this.disconnect());
  }

  /** Troca o tópico assinado; o anterior para de chegar. */
  follow(topic: string, onBatch: (quotes: Quote[]) => void): void {
    this.watch = { topic, onBatch };
    this.ensureClient();
    if (this.client?.connected) {
      this.subscribe();
    }
  }

  stop(): void {
    this.watch = null;
    this.subscription?.unsubscribe();
    this.subscription = null;
  }

  disconnect(): void {
    this.stop();
    void this.client?.deactivate();
    this.client = null;
    this.state.set('disconnected');
  }

  private ensureClient(): void {
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
      this.subscribe();
    };
    client.onWebSocketClose = () => this.state.set('connecting');
    this.state.set('connecting');
    client.activate();
    this.client = client;
  }

  private subscribe(): void {
    this.subscription?.unsubscribe();
    this.subscription = null;
    const watch = this.watch;
    if (!watch || !this.client) {
      return;
    }
    this.subscription = this.client.subscribe(watch.topic, (message) => {
      const payload = JSON.parse(message.body) as Quote | Quote[];
      watch.onBatch(Array.isArray(payload) ? payload : [payload]);
    });
  }
}
