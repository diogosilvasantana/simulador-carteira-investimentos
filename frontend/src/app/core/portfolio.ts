import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, catchError, tap, throwError } from 'rxjs';

/** Formatos de /api/portfolio. Dinheiro e percentuais chegam como texto decimal. */
export type AssetClassSlug = 'acoes' | 'renda-fixa' | 'fiagros' | 'fiis' | 'bdrs' | 'etfs';
export type OrderSide = 'BUY' | 'SELL';

export interface Allocation {
  readonly assetClass: AssetClassSlug;
  readonly value: string;
  readonly percent: string;
}

export interface PositionView {
  readonly ticker: string;
  readonly name: string;
  readonly assetClass: AssetClassSlug;
  readonly quantity: number;
  readonly averagePrice: string;
  readonly lastPrice: string | null;
  readonly changePercent: string | null;
  readonly marketValue: string;
  readonly unrealizedPnl: string;
  readonly unrealizedPnlPercent: string | null;
  readonly weightPercent: string;
}

export interface FixedIncomeView {
  readonly id: number;
  readonly productId: string;
  readonly name: string;
  readonly principal: string;
  readonly gross: string;
  readonly net: string;
  readonly start: string;
  readonly ratesAvailable: boolean;
}

export interface ExecutionView {
  readonly id: number;
  readonly side: OrderSide;
  readonly ticker: string;
  readonly quantity: number;
  readonly price: string;
  readonly fees: string;
  readonly total: string;
  readonly executedAt: string;
}

export interface PortfolioSummary {
  readonly total: string;
  readonly cash: string;
  readonly contributed: string;
  readonly result: string;
  readonly resultPercent: string | null;
  readonly unrealizedPnl: string;
  readonly realizedPnl: string;
  readonly allocation: readonly Allocation[];
  readonly positions: readonly PositionView[];
  readonly fixedIncome: readonly FixedIncomeView[];
  readonly recentExecutions: readonly ExecutionView[];
}

export interface OrderRequest {
  readonly ticker: string;
  readonly side: OrderSide;
  readonly quantity: number;
}

export interface OrderQuote {
  readonly side: OrderSide;
  readonly ticker: string;
  readonly quantity: number;
  readonly price: string;
  readonly gross: string;
  readonly fees: string;
  readonly total: string;
}

const API = '/api/portfolio';

/** Estado da carteira compartilhado entre telas; toda operação devolve e atualiza o resumo. */
@Injectable({ providedIn: 'root' })
export class PortfolioService {
  private readonly http = inject(HttpClient);

  readonly summary = signal<PortfolioSummary | null>(null);
  readonly unavailable = signal(false);

  load(): void {
    this.http.get<PortfolioSummary>(API).subscribe({
      next: (summary) => {
        this.summary.set(summary);
        this.unavailable.set(false);
      },
      error: () => this.unavailable.set(true),
    });
  }

  contribute(amount: string): Observable<PortfolioSummary> {
    return this.http.post<PortfolioSummary>(`${API}/contributions`, { amount }).pipe(
      tap((summary) => this.summary.set(summary)),
      catchError(problem),
    );
  }

  preview(order: OrderRequest): Observable<OrderQuote> {
    return this.http.post<OrderQuote>(`${API}/orders/preview`, order).pipe(catchError(problem));
  }

  order(order: OrderRequest): Observable<ExecutionView> {
    return this.http.post<ExecutionView>(`${API}/orders`, order).pipe(
      tap(() => this.load()),
      catchError(problem),
    );
  }

  invest(productId: string, amount: string): Observable<FixedIncomeView> {
    return this.http.post<FixedIncomeView>(`${API}/fixed-income`, { productId, amount }).pipe(
      tap(() => this.load()),
      catchError(problem),
    );
  }

  reset(): Observable<void> {
    return this.http.delete<void>(API).pipe(
      tap(() => this.load()),
      catchError(problem),
    );
  }
}

/** Erro da API já com a frase para a tela (o backend responde em Problem Details). */
export class PortfolioError extends Error {}

function problem(error: HttpErrorResponse): Observable<never> {
  const body = error.error as { title?: string; detail?: string } | null;
  const message =
    error.status === 0
      ? 'O backend não respondeu. Ele está rodando?'
      : (body?.detail ?? body?.title ?? 'Não foi possível concluir a operação.');
  return throwError(() => new PortfolioError(message));
}
