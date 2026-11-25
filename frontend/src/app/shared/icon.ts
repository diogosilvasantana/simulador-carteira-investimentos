import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

// Ícones de traço desenhados para o produto: 24 × 24, traço 1,75, pontas arredondadas.
const PATHS = {
  home: 'M3 10.5 12 3l9 7.5V20a1 1 0 0 1-1 1h-5v-6h-6v6H4a1 1 0 0 1-1-1z',
  quotes: 'M4 19V9m6 10V5m6 14v-8m6 8H2',
  fixedIncome: 'M3 9 12 4l9 5M5 10v8m4.5-8v8m5-8v8M19 10v8M3 20h18',
  collapse: 'M15 6l-6 6 6 6',
  expand: 'M9 6l6 6-6 6',
  search: 'M11 4a7 7 0 1 0 0 14 7 7 0 0 0 0-14zm9 16-4.3-4.3',
  arrowUp: 'M12 19V5m-6 6 6-6 6 6',
  arrowDown: 'M12 5v14m6-6-6 6-6-6',
  close: 'M6 6l12 12M18 6 6 18',
  check: 'M5 12.5l4.5 4.5L19 7',
  wallet:
    'M4 7h15a1 1 0 0 1 1 1v11a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1zM4 7V6a2 2 0 0 1 2-2h11M16 13.5h.01',
  radar:
    'M12 12m-8 0a8 8 0 1 0 16 0 8 8 0 1 0-16 0M12 12m-4 0a4 4 0 1 0 8 0 4 4 0 1 0-8 0M12 12l6-6',
  star: 'M12 3.5l2.6 5.3 5.9.9-4.2 4.1 1 5.8-5.3-2.8-5.3 2.8 1-5.8-4.2-4.1 5.9-.9z',
  plus: 'M12 5v14M5 12h14',
  build: 'M4 20h16M6 16V10m4 6V6m4 10v-4m4 4V8',
} as const;

export type IconName = keyof typeof PATHS;

@Component({
  selector: 'app-icon',
  template: `<svg
    [attr.width]="size()"
    [attr.height]="size()"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="1.75"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
  >
    <path [attr.d]="path()" />
  </svg>`,
  styles: ':host { display: inline-flex; flex: none; }',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Icon {
  readonly name = input.required<IconName>();
  readonly size = input(20);
  protected readonly path = computed(() => PATHS[this.name()]);
}
