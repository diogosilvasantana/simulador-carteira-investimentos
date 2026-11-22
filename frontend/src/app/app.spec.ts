import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('should show the simulated-mode badge', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();

    const badge = (fixture.nativeElement as HTMLElement).querySelector('.badge');

    expect(badge?.textContent).toContain('SIMULADO');
  });
});
