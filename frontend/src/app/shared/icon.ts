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
