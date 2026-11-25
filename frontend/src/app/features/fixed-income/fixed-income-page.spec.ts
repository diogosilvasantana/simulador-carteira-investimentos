import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { FixedIncomeProduct, Simulation } from './fixed-income';
import { FixedIncomePage } from './fixed-income-page';

const CDB: FixedIncomeProduct = {
  id: 'cdb-110',
  name: 'CDB 110% do CDI',
  kind: 'CDB',
  indexPercentage: '110',
  incomeTaxExempt: false,
  gracePeriodDays: 0,
};

const LCI: FixedIncomeProduct = {
  id: 'lci-92',
  name: 'LCI 92% do CDI',
  kind: 'LCI',
  indexPercentage: '92',
  incomeTaxExempt: true,
  gracePeriodDays: 90,
};

function aSimulation(overrides: Partial<Simulation>): Simulation {
  return {
    principal: '10000.00',
    gross: '11500.00',
    grossYield: '1500.00',
    iof: '0.00',
    incomeTax: '262.50',
    net: '11237.50',
    holdingDays: 365,
    iofRatePercent: '0',
    incomeTaxRatePercent: '17.5',
    ratesUntil: '2026-11-23',
    projectedBusinessDays: 250,
    ...overrides,
  };
}

describe('FixedIncomePage', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
  });

  async function open(): Promise<{ page: HTMLElement; http: HttpTestingController }> {
    const fixture = TestBed.createComponent(FixedIncomePage);
    fixture.detectChanges();
    await fixture.whenStable();
    return {
      page: fixture.nativeElement as HTMLElement,
      http: TestBed.inject(HttpTestingController),
    };
  }

  it('should show the annual CDI and compare every product by net amount', async () => {
    const { page, http } = await open();

    http.expectOne('/api/fixed-income/indexes').flush([
      {
        index: 'CDI',
        date: '2026-11-23',
        percent: '0.055131',
        unit: '% ao dia',
        annualPercent: '14.90',
      },
    ]);
    http.expectOne('/api/fixed-income/products').flush([LCI, CDB]);
    const simulations = http.match('/api/fixed-income/simulations');
    expect(simulations.map((r) => r.request.body.indexPercentage)).toEqual(['92', '110']);
    expect(simulations[0].request.body.incomeTaxExempt).toBe(true);
    simulations[0].flush(aSimulation({ net: '11300.00', incomeTax: '0.00' }));
    simulations[1].flush(aSimulation({ net: '11237.50' }));
    TestBed.tick();

    expect(page.textContent).toContain('14,90% ao ano');
    const rows = Array.from(page.querySelectorAll('tbody tr')).map((tr) => tr.textContent ?? '');
    expect(rows[0]).toContain('LCI 92% do CDI');
    expect(rows[0]).toContain('+1.300,00');
    expect(rows[1]).toContain('CDB 110% do CDI');
  });

  it('should flag a product whose grace period the term does not meet', async () => {
    const { page, http } = await open();

    http.expectOne('/api/fixed-income/indexes').flush([]);
    http.expectOne('/api/fixed-income/products').flush([LCI]);
    http.expectOne('/api/fixed-income/simulations').flush(aSimulation({ holdingDays: 30 }));
    TestBed.tick();

    expect(page.textContent).toContain('carência de 90 dias');
  });

  it('should explain that the indexes are still loading when the backend answers 503', async () => {
    const { page, http } = await open();

    http.expectOne('/api/fixed-income/indexes').flush([]);
    http.expectOne('/api/fixed-income/products').flush([CDB]);
    http
      .expectOne('/api/fixed-income/simulations')
      .flush(null, { status: 503, statusText: 'Service Unavailable' });
    TestBed.tick();

    expect(page.textContent).toContain('ainda estão chegando do Banco Central');
  });
});
