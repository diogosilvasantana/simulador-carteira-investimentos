import { ChangeDetectionStrategy, Component, ElementRef, input, viewChild } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Icon } from './icon';

export interface TypeTile {
  readonly id: string;
  readonly link: readonly string[];
  readonly label: string;
  readonly description: string;
  readonly color: string;
  /** Texto do rodapé, como "452 ativos"; vazio enquanto a contagem não chega. */
  readonly footnote?: string;
}

/** O carrossel de tipos de ativo: cada card leva à lista daquela classe. */
@Component({
  selector: 'app-type-carousel',
  imports: [RouterLink, Icon],
  template: `
    <div class="nav" aria-hidden="true">
      <button type="button" tabindex="-1" (click)="scroll(-1)" aria-label="Anteriores">
        <app-icon name="collapse" [size]="18" />
      </button>
      <button type="button" tabindex="-1" (click)="scroll(1)" aria-label="Próximos">
        <app-icon name="expand" [size]="18" />
      </button>
    </div>
    <ul #track class="track" [attr.aria-label]="label()">
      @for (tile of tiles(); track tile.id) {
        <li>
          <a
            class="tile"
            [class.tile--active]="tile.id === active()"
            [routerLink]="tile.link"
            [attr.aria-current]="tile.id === active() ? 'page' : null"
          >
            <span class="tile__mark" [style.background]="tile.color"></span>
            <span class="tile__label">{{ tile.label }}</span>
            <span class="tile__desc">{{ tile.description }}</span>
            <span class="tile__foot num">{{ tile.footnote ?? '' }}</span>
          </a>
        </li>
      }
    </ul>
  `,
  styleUrl: './type-carousel.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TypeCarousel {
  readonly tiles = input.required<readonly TypeTile[]>();
  readonly active = input<string | null>(null);
  readonly label = input('Tipos de ativo');

  private readonly track = viewChild.required<ElementRef<HTMLElement>>('track');

  protected scroll(direction: 1 | -1): void {
    const el = this.track().nativeElement;
    el.scrollBy({ left: direction * el.clientWidth * 0.8, behavior: 'smooth' });
  }
}
