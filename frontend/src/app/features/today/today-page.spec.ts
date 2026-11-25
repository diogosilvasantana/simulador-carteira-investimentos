import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Snapshot } from './today';
import { TodayPage } from './today-page';

const SNAPSHOT: Snapshot = {
  income: '5000.00',
  essentials: '4000.00',
  others: '0.00',
  installments: '1200.00',
  totalDebt: '9000.00',
  available: '-200.00',
  stage: 'REORGANIZE',
  incompleteDebts: 1,
  items: [{ id: 1, name: 'Salário', kind: 'INCOME', amount: '5000.00' }],
  debts: [
    {
      id: 7,
      creditor: 'Banco A',
      balance: '9000.00',
      monthlyRatePercent: null,
      installment: '1200.00',
      overdue: true,
      collateral: true,
    },
  ],
};

describe('TodayPage', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TodayPage],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });

  function open() {
    const fixture = TestBed.createComponent(TodayPage);
    TestBed.tick();
    return fixture.nativeElement as HTMLElement;
  }

  it('should show the stage, the shortfall and what comes first', () => {
    const page = open();
    http.expectOne('/api/personal-finance').flush(SNAPSHOT);
    TestBed.tick();

    expect(page.textContent).toContain('Proteger a casa e reorganizar');
    expect(page.textContent).toContain('Falta no mês');
    expect(page.textContent).toContain('−R$ 200,00');
    expect(page.textContent).toContain('Vencida e com bem em garantia');
  });

  it('should only remove after a second click', () => {
    const page = open();
    http.expectOne('/api/personal-finance').flush(SNAPSHOT);
    TestBed.tick();
    const button = page.querySelector<HTMLButtonElement>('[aria-label="Remover Banco A"]')!;

    button.click();
    http.expectNone('/api/personal-finance/debts/7');
    button.click();

    expect(http.expectOne('/api/personal-finance/debts/7').request.method).toBe('DELETE');
  });

  it('should explain when the personal area is off', () => {
    const page = open();
    http.expectOne('/api/personal-finance').flush('', { status: 404, statusText: 'Not Found' });
    TestBed.tick();

    expect(page.textContent).toContain('só funcionam no modo local');
  });
});
