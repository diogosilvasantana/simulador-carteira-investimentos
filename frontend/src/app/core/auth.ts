import { HttpClient, HttpErrorResponse, HttpInterceptorFn, HttpParams } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Observable, catchError, map, of, tap, throwError } from 'rxjs';

interface Me {
  readonly username: string;
}

export type LoginFailure = 'credentials' | 'blocked' | 'unavailable';

/**
 * Sessão do dono da carteira. A credencial fica num cookie HttpOnly que o JavaScript não lê;
 * aqui só sabemos se há alguém logado e quem é.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  /** undefined enquanto não perguntamos ao backend; null quando não há sessão. */
  readonly user = signal<string | null | undefined>(undefined);

  check(): Observable<boolean> {
    const known = this.user();
    if (known !== undefined) {
      return of(known !== null);
    }
    return this.http.get<Me>('/api/auth/me').pipe(
      tap((me) => this.user.set(me.username)),
      map(() => true),
      catchError(() => {
        this.user.set(null);
        return of(false);
      }),
    );
  }

  login(username: string, password: string): Observable<void> {
    const body = new HttpParams().set('username', username).set('password', password);
    return this.http
      .post<void>('/api/auth/login', body, {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      })
      .pipe(
        tap(() => this.user.set(username)),
        catchError((error: HttpErrorResponse) => throwError(() => failure(error))),
      );
  }

  logout(): void {
    this.http.post<void>('/api/auth/logout', null).subscribe({
      complete: () => this.leave(),
      error: () => this.leave(),
    });
  }

  /** Sessão acabou (saída ou expiração): volta ao login lembrando onde a pessoa estava. */
  expired(returnTo?: string): void {
    this.user.set(null);
    void this.router.navigate(['/login'], {
      queryParams: returnTo && returnTo !== '/login' ? { voltar: returnTo } : {},
    });
  }

  private leave(): void {
    this.user.set(null);
    void this.router.navigate(['/login']);
  }
}

function failure(error: HttpErrorResponse): LoginFailure {
  if (error.status === 401) {
    return 'credentials';
  }
  return error.status === 429 ? 'blocked' : 'unavailable';
}

/** Telas internas só com sessão; sem ela, vai para o login e volta depois. */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth
    .check()
    .pipe(
      map((ok) => ok || router.createUrlTree(['/login'], { queryParams: { voltar: state.url } })),
    );
};

/** Uma chamada que volta 401 no meio do uso significa sessão expirada. */
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return next(request).pipe(
    catchError((error: unknown) => {
      if (
        error instanceof HttpErrorResponse &&
        error.status === 401 &&
        !request.url.startsWith('/api/auth/')
      ) {
        auth.expired(router.url);
      }
      return throwError(() => error);
    }),
  );
};
