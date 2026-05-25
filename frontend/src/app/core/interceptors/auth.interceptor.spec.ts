import { HttpClient, HttpErrorResponse, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';
import { NotificationService } from '../services/notification.service';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let httpMock: HttpTestingController;
  let authService: jasmine.SpyObj<AuthService>;
  let notificationService: jasmine.SpyObj<NotificationService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['getToken', 'logout']);
    notificationService = jasmine.createSpyObj<NotificationService>('NotificationService', ['error']);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authService },
        { provide: NotificationService, useValue: notificationService },
        { provide: Router, useValue: router }
      ]
    });

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should send Authorization Bearer header to protected API requests', () => {
    authService.getToken.and.returnValue('jwt-token');
    const http = TestBed.inject(HttpClient);

    http.get(`${environment.apiUrl}/organizations`).subscribe();

    const request = httpMock.expectOne(`${environment.apiUrl}/organizations`);
    expect(request.request.headers.get('Authorization')).toBe('Bearer jwt-token');
    request.flush([]);
  });

  it('should add bearer token and force logout on 401', () => {
    authService.getToken.and.returnValue('jwt-token');
    const http = TestBed.inject(HttpClient);
    let responseError: HttpErrorResponse | undefined;

    http.get(`${environment.apiUrl}/organizations`).subscribe({
      error: (error: HttpErrorResponse) => {
        responseError = error;
      }
    });

    const request = httpMock.expectOne(`${environment.apiUrl}/organizations`);
    expect(request.request.headers.get('Authorization')).toBe('Bearer jwt-token');
    request.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });

    expect(responseError?.status).toBe(401);
    expect(authService.logout).toHaveBeenCalledWith(false);
    expect(notificationService.error).toHaveBeenCalledWith('Sua sessao expirou. Faca login novamente.');
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should not force logout when login request returns 401', () => {
    authService.getToken.and.returnValue(null);
    const http = TestBed.inject(HttpClient);
    let responseError: HttpErrorResponse | undefined;

    http.post(`${environment.apiUrl}/auth/login`, { email: 'manager@rodojacto.com', password: 'wrong' }).subscribe({
      error: (error: HttpErrorResponse) => {
        responseError = error;
      }
    });

    const request = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(request.request.headers.has('Authorization')).toBeFalse();
    request.flush({ message: 'Invalid credentials' }, { status: 401, statusText: 'Unauthorized' });

    expect(responseError?.status).toBe(401);
    expect(authService.logout).not.toHaveBeenCalled();
    expect(notificationService.error).not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
