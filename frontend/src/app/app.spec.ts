import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('should show the simulated-mode badge', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();

    const badge = (fixture.nativeElement as HTMLElement).querySelector('.sim');

    expect(badge?.textContent).toContain('SIMULADO');
  });

  it('should collapse the side menu and remember the choice', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const host = fixture.nativeElement as HTMLElement;

    host.querySelector<HTMLButtonElement>('.nav__toggle')!.click();
    TestBed.tick();

    expect(host.classList).toContain('collapsed');
    expect(localStorage.getItem('simulador.nav.collapsed')).toBe('true');
  });
});
