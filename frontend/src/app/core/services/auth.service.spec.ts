import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';

import { environment } from '../../../environments/environment';
import { CurrentUser } from '../models/auth.model';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let router: Router;

  const currentUser: CurrentUser = {
    id: 1,
    fullName: 'Manager',
    email: 'manager@rodojacto.com',
    accessLevel: 'MANAGER',
    organizationId: 10,
    organizationName: 'Rodojacto Matriz',
    createdAt: '2026-01-01T10:00:00'
  };

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should save token and fetch current user after login', () => {
    let emittedUser: CurrentUser | undefined;

    service.login({ email: 'manager@rodojacto.com', password: '123456' }).subscribe((user) => {
      emittedUser = user;
    });

    const loginRequest = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(loginRequest.request.method).toBe('POST');
    loginRequest.flush({ accessToken: 'jwt-token', tokenType: 'Bearer' });

    expect(localStorage.getItem('rodojacto_token')).toBe('jwt-token');

    const meRequest = httpMock.expectOne(`${environment.apiUrl}/auth/me`);
    expect(meRequest.request.method).toBe('GET');
    meRequest.flush(currentUser);

    expect(emittedUser).toEqual(currentUser);
    expect(service.currentUser()).toEqual(currentUser);
  });

  it('should remove token and clear current user on logout', () => {
    localStorage.setItem('rodojacto_token', 'jwt-token');

    service.logout();

    expect(localStorage.getItem('rodojacto_token')).toBeNull();
    expect(service.currentUser()).toBeNull();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should call auth me when getting current user with token and empty state', () => {
    let emittedUser: CurrentUser | null | undefined;
    localStorage.setItem('rodojacto_token', 'jwt-token');

    service.getCurrentUser().subscribe((user) => {
      emittedUser = user;
    });

    const meRequest = httpMock.expectOne(`${environment.apiUrl}/auth/me`);
    expect(meRequest.request.method).toBe('GET');
    meRequest.flush(currentUser);

    expect(emittedUser).toEqual(currentUser);
    expect(service.currentUser()).toEqual(currentUser);
  });
});
