import { ChangeDetectionStrategy, Component, inject, input, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService, LoginFailure } from '../../core/auth';
import { BrandMark } from '../../shared/brand-mark';

const MESSAGES: Record<LoginFailure, string> = {
  credentials: 'Usuário ou senha incorretos.',
  blocked: 'Muitas tentativas erradas. Espere 15 minutos e tente de novo.',
  unavailable: 'Não foi possível entrar agora. Confira se o servidor está no ar.',
};

@Component({
  selector: 'app-login-page',
  imports: [BrandMark],
  templateUrl: './login-page.html',
  styleUrl: './login-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginPage {
  /** Página que pediu login, para voltar a ela depois. */
  readonly voltar = input<string>();

  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly username = signal('');
  protected readonly password = signal('');
  protected readonly pending = signal(false);
  protected readonly error = signal<string | null>(null);

  protected submit(event: Event): void {
    event.preventDefault();
    if (this.pending() || !this.username() || !this.password()) {
      return;
    }
    this.pending.set(true);
    this.error.set(null);
    this.auth.login(this.username().trim(), this.password()).subscribe({
      next: () => {
        // Só caminhos internos: um ?voltar= externo não pode levar a outro site.
        const target = this.voltar();
        void this.router.navigateByUrl(
          target?.startsWith('/') && !target.startsWith('//') ? target : '/',
        );
      },
      error: (failure: LoginFailure) => {
        this.pending.set(false);
        this.password.set('');
        this.error.set(MESSAGES[failure]);
      },
    });
  }
}
