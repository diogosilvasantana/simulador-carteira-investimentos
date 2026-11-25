import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Home } from './home';

describe('Home', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
  });

  async function open(): Promise<HTMLElement> {
    const fixture = TestBed.createComponent(Home);
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  it('should mark what is available and what is still being built', async () => {
    const page = await open();

    const items = page.querySelectorAll('.phases li');

    expect(items.length).toBe(5);
    expect(items[0].textContent).toContain('Disponível');
    expect(items[2].textContent).toContain('Em construção');
  });

  it('should show the asset count of each type in the carousel', async () => {
    const page = await open();

    TestBed.inject(HttpTestingController)
      .expectOne('/api/categories')
      .flush([{ slug: 'acoes', count: 452 }]);
    TestBed.tick();

    expect(page.querySelector('.tile')?.textContent).toContain('452 ativos');
  });
});
