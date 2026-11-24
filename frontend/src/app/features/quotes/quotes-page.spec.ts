import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { QuoteStream } from './quote-stream';
import { QuotesPage } from './quotes-page';

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

  it('should render the initial snapshot with Brazilian number format', async () => {
    const fixture = TestBed.createComponent(QuotesPage);
    fixture.detectChanges();

    TestBed.inject(HttpTestingController)
      .expectOne('/api/quotes')
      .flush([
        {
          ticker: 'BOVA11',
          last: '1128.4',
          bid: null,
          ask: '1128.41',
          volume: 300,
          occurredAt: '2026-11-24T13:00:00Z',
          source: 'LIVE',
        },
      ]);
    await fixture.whenStable();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('BOVA11');
    expect(text).toContain('1.128,40');
    expect(text).toContain('Ao vivo');
    expect(connect).toHaveBeenCalledOnce();
  });

  it('should show a hint while there are no quotes', async () => {
    const fixture = TestBed.createComponent(QuotesPage);
    fixture.detectChanges();

    TestBed.inject(HttpTestingController).expectOne('/api/quotes').flush([]);
    await fixture.whenStable();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Nenhuma cotação ainda');
  });
});
