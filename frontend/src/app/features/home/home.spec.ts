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

  async function open(): Promise<{ page: HTMLElement; http: HttpTestingController }> {
    const fixture = TestBed.createComponent(Home);
    await fixture.whenStable();
    return {
      page: fixture.nativeElement as HTMLElement,
      http: TestBed.inject(HttpTestingController),
    };
  }

  it('should invite to build a portfolio while nothing was contributed', async () => {
    const { page, http } = await open();

    http.expectOne('/api/portfolio').flush({ contributed: '0', allocation: [] });
    TestBed.tick();

    expect(page.querySelector('.intro a')?.getAttribute('href')).toBe('/carteira/montar');
  });

  it('should summarize the portfolio once there is a contribution', async () => {
    const { page, http } = await open();

    http.expectOne('/api/portfolio').flush({
      total: '10250.00',
      cash: '250.00',
      contributed: '10000.00',
      result: '250.00',
      resultPercent: '2.50',
      allocation: [{ assetClass: 'acoes', value: '10000.00', percent: '97.56' }],
    });
    TestBed.tick();

    expect(page.querySelector('.summary')?.textContent).toContain('R$ 10.250,00');
    expect(page.querySelector('.summary')?.textContent).toContain('+R$ 250,00');
  });

  it('should list the biggest gainers of the day', async () => {
    const { page, http } = await open();

    http.expectOne('/api/quotes').flush([
      { ticker: 'PETR4', type: 'STOCK', last: '38.00', changePercent: '2.10', volume: 10 },
      { ticker: 'VALE3', type: 'STOCK', last: '60.00', changePercent: '-1.00', volume: 20 },
    ]);
    TestBed.tick();

    expect(page.querySelector('#movers-gainers')?.parentElement?.textContent).toContain('PETR4');
    expect(page.querySelector('#movers-gainers')?.parentElement?.textContent).not.toContain(
      'VALE3',
    );
  });

  it('should show the asset count of each type in the carousel', async () => {
    const { page, http } = await open();

    http.expectOne('/api/categories').flush([{ slug: 'acoes', count: 452 }]);
    TestBed.tick();

    expect(page.querySelector('.tile')?.textContent).toContain('452 ativos');
  });
});
