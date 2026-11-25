import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Quote } from './quote';
import { QuoteStream } from './quote-stream';
import { QuotesPage } from './quotes-page';

function quote(ticker: string, last: string, changePercent: string | null): Quote {
  return {
    ticker,
    last,
    bid: null,
    ask: null,
    previousClose: changePercent === null ? null : '10.00',
    changePercent,
    volume: 300,
    occurredAt: '2026-11-24T13:00:00Z',
    source: 'LIVE',
  };
}

describe('QuotesPage', () => {
  const connect = vi.fn();

  beforeEach(() => {
    connect.mockReset();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: QuoteStream, useValue: { connect, state: () => 'connected' } },
      ],
    });
  });

  async function render(quotes: Quote[]): Promise<HTMLElement> {
    const fixture = TestBed.createComponent(QuotesPage);
    fixture.detectChanges();
    TestBed.inject(HttpTestingController).expectOne('/api/quotes').flush(quotes);
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  function tickers(page: HTMLElement): string[] {
    return [...page.querySelectorAll('tbody th')].map((cell) => cell.textContent?.trim() ?? '');
  }

  it('should render prices and change in Brazilian format', async () => {
    const page = await render([quote('BOVA11', '1128.4', '1.25')]);

    const text = page.textContent ?? '';
    expect(text).toContain('1.128,40');
    expect(text).toContain('+1,25%');
    expect(text).toContain('Ao vivo');
    expect(page.querySelector('.change--up')).not.toBeNull();
    expect(connect).toHaveBeenCalledOnce();
  });

  it('should sort by change with the biggest gain first and unknown change last', async () => {
    const page = await render([
      quote('ABEV3', '13.20', '-2.10'),
      quote('PETR4', '38.47', '3.97'),
      quote('AAAA3', '5.00', null),
      quote('VALE3', '61.20', '0.50'),
    ]);

    (page.querySelectorAll('button.sort')[1] as HTMLButtonElement).click();
    TestBed.tick();

    expect(tickers(page)).toEqual(['PETR4', 'VALE3', 'ABEV3', 'AAAA3']);
  });

  it('should filter by ticker as the user types', async () => {
    const page = await render([quote('PETR3', '40.00', null), quote('VALE3', '61.20', null)]);
    const search = page.querySelector('input.search') as HTMLInputElement;

    search.value = 'pet';
    search.dispatchEvent(new Event('input'));
    TestBed.tick();

    expect(tickers(page)).toEqual(['PETR3']);
    expect(page.textContent).toContain('1 de 2 ativos');
  });

  it('should show a hint while there are no quotes', async () => {
    const page = await render([]);

    expect(page.textContent).toContain('Nenhuma cotação ainda');
  });
});
