import { TestBed } from '@angular/core/testing';
import { Home } from './home';

describe('Home', () => {
  it('should list the roadmap phases', async () => {
    const fixture = TestBed.createComponent(Home);
    await fixture.whenStable();

    const items = (fixture.nativeElement as HTMLElement).querySelectorAll('.phases li');

    expect(items.length).toBe(5);
    expect(items[0].textContent).toContain('Núcleo');
  });
});
