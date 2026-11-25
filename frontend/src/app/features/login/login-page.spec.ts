import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { LoginPage } from './login-page';

describe('LoginPage', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
  });

  async function open(voltar?: string): Promise<HTMLElement> {
    const fixture = TestBed.createComponent(LoginPage);
    if (voltar) {
      fixture.componentRef.setInput('voltar', voltar);
    }
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  function fill(page: HTMLElement, user: string, password: string): void {
    const [userInput, passwordInput] = Array.from(page.querySelectorAll('input'));
    userInput.value = user;
    userInput.dispatchEvent(new Event('input'));
    passwordInput.value = password;
    passwordInput.dispatchEvent(new Event('input'));
    page.querySelector('form')?.dispatchEvent(new Event('submit'));
    TestBed.tick();
  }

  it('should send the credentials as a form and go back to the page that asked', async () => {
    const page = await open('/carteira');
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);

    fill(page, 'diogo', 'segredo');
    const request = TestBed.inject(HttpTestingController).expectOne('/api/auth/login');
    request.flush(null, { status: 204, statusText: 'No Content' });

    expect(request.request.body.toString()).toBe('username=diogo&password=segredo');
    expect(navigate).toHaveBeenCalledWith('/carteira');
  });

  it('should never follow a return address to another site', async () => {
    const page = await open('//evil.example');
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);

    fill(page, 'diogo', 'segredo');
    TestBed.inject(HttpTestingController)
      .expectOne('/api/auth/login')
      .flush(null, { status: 204, statusText: 'No Content' });

    expect(navigate).toHaveBeenCalledWith('/');
  });

  it('should say when the password is wrong and clear it', async () => {
    const page = await open();

    fill(page, 'diogo', 'errada');
    TestBed.inject(HttpTestingController)
      .expectOne('/api/auth/login')
      .flush(null, { status: 401, statusText: 'Unauthorized' });
    TestBed.tick();

    expect(page.querySelector('[role="alert"]')?.textContent).toContain('incorretos');
    expect(page.querySelectorAll('input')[1].value).toBe('');
  });

  it('should explain the lockout after too many attempts', async () => {
    const page = await open();

    fill(page, 'diogo', 'errada');
    TestBed.inject(HttpTestingController)
      .expectOne('/api/auth/login')
      .flush(null, { status: 429, statusText: 'Too Many Requests' });
    TestBed.tick();

    expect(page.querySelector('[role="alert"]')?.textContent).toContain('15 minutos');
  });
});
