import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { WatchlistService } from '../../shared/watchlist';
import { RadarPage } from './radar-page';

describe('RadarPage', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
  });

  async function open(): Promise<HTMLElement> {
    const fixture = TestBed.createComponent(RadarPage);
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  async function submit(page: HTMLElement, ticker: string): Promise<void> {
    const input = page.querySelector('input') as HTMLInputElement;
    input.value = ticker;
    input.dispatchEvent(new Event('input'));
    page.querySelector('form')?.dispatchEvent(new Event('submit'));
    TestBed.tick();
  }

  it('should reject a code that is not a ticker', async () => {
    const page = await open();

    await submit(page, 'petrobras');

    expect(page.querySelector('[role="alert"]')?.textContent).toContain('código de negociação');
  });

  it('should add a known asset to the radar', async () => {
    const page = await open();

    await submit(page, 'petr4');
    TestBed.inject(HttpTestingController)
      .expectOne('/api/assets/PETR4')
      .flush({ ticker: 'PETR4', name: 'PETROBRAS PN', last: '38.00', changePercent: '1.00' });
    TestBed.tick();

    expect(TestBed.inject(WatchlistService).tickers()).toEqual(['PETR4']);
    expect(page.querySelector('.card')?.textContent).toContain('R$ 38,00');
  });
});
