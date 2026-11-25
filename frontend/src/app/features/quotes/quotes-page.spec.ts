import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { QuoteStream } from './quote-stream';
import { QuotesPage } from './quotes-page';
import { aQuote } from './testing';

describe('QuotesPage', () => {
  const follow = vi.fn();

  beforeEach(() => {
    follow.mockReset();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: QuoteStream, useValue: { follow, stop: vi.fn(), state: () => 'connected' } },
      ],
    });
  });

  async function open(category: string): Promise<HTMLElement> {
    const fixture = TestBed.createComponent(QuotesPage);
    fixture.componentRef.setInput('categoria', category);
    fixture.detectChanges();
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  it('should load only the open category and follow its topic after the snapshot', async () => {
    const page = await open('fiis');
    const http = TestBed.inject(HttpTestingController);

    http.expectOne('/api/categories').flush([
      { slug: 'acoes', count: 450 },
      { slug: 'fiis', count: 2 },
    ]);
    expect(follow).not.toHaveBeenCalled();
    http
      .expectOne((req) => req.url === '/api/assets' && req.params.get('category') === 'fiis')
      .flush([
        aQuote({ ticker: 'MXRF11', type: 'REAL_ESTATE_FUND' }),
        aQuote({ ticker: 'HGLG11' }),
      ]);
    TestBed.tick();

    expect(follow).toHaveBeenCalledWith('/topic/quotes/category/fiis', expect.any(Function));
    expect(page.textContent).toContain('2 ativos');
    expect(page.textContent).toContain('450');
  });

  it('should tell the user when the category is unknown', async () => {
    const page = await open('cripto');
    TestBed.inject(HttpTestingController).expectOne('/api/categories').flush([]);

    expect(page.textContent).toContain('Categoria desconhecida');
  });
});
