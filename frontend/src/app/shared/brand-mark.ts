import { ChangeDetectionStrategy, Component } from '@angular/core';

/** Marca provisória: quadrado índigo com a linha de um gráfico subindo. */
@Component({
  selector: 'app-brand-mark',
  template: `<svg
    width="16"
    height="16"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="2.5"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
  >
    <path d="M4 17l5-6 4 4 7-9" />
  </svg>`,
  styles: `
    :host {
      display: grid;
      place-items: center;
      flex: none;
      width: 32px;
      height: 32px;
      border-radius: var(--radius-sm);
      background: var(--accent-strong);
      color: var(--on-accent);
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BrandMark {}
