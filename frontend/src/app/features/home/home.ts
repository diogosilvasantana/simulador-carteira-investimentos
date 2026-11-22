import { ChangeDetectionStrategy, Component, signal } from '@angular/core';

interface Phase {
  readonly name: string;
  readonly description: string;
}

@Component({
  selector: 'app-home',
  templateUrl: './home.html',
  styleUrl: './home.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Home {
  protected readonly phases = signal<readonly Phase[]>([
    { name: 'Núcleo', description: 'Carteiras, ordens a mercado, posição e preço médio' },
    { name: 'Tempo real', description: 'Ponte MT5, WebSocket, replay e home broker' },
    { name: 'Proventos', description: 'Dividendos, JCP e desdobramentos' },
    { name: 'Rentabilidade', description: 'TWR contra Ibovespa e CDI' },
    { name: 'Rebalanceamento', description: 'Alocação-alvo e sugestão de aportes' },
  ]);
}
