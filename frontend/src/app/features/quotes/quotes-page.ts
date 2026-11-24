import { HttpClient } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Quote } from './quote';
import { QuoteStore } from './quote-store';
import { QuoteStream } from './quote-stream';

@Component({
  selector: 'app-quotes-page',
  imports: [DatePipe],
  templateUrl: './quotes-page.html',
  styleUrl: './quotes-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class QuotesPage implements OnInit {
  private readonly http = inject(HttpClient);
  protected readonly store = inject(QuoteStore);
  protected readonly stream = inject(QuoteStream);

  protected readonly stateLabel = {
    connected: 'Ao vivo',
    connecting: 'Conectando…',
    disconnected: 'Desconectado',
  } as const;

  protected readonly sourceLabel = {
    LIVE: 'MT5',
    REPLAY: 'Replay',
    SYNTHETIC: 'Sintético',
  } as const;

  ngOnInit(): void {
    // Carrega o retrato atual antes de assinar, para a tabela não começar vazia.
    this.http.get<Quote[]>('/api/quotes').subscribe({
      next: (quotes) => {
        this.store.load(quotes);
        this.stream.connect((quote) => this.store.apply(quote));
      },
      error: () => this.stream.connect((quote) => this.store.apply(quote)),
    });
  }

  protected formatPrice(value: string | null): string {
    if (value === null) {
      return '—';
    }
    const [integer, fraction = ''] = value.split('.');
    const grouped = integer.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
    return `${grouped},${fraction.padEnd(2, '0')}`;
  }
}
