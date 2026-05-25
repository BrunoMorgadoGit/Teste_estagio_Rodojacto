import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { throwError } from 'rxjs';

import { ApiErrorService } from '../../core/services/api-error.service';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let component: LoginComponent;
  let authService: jasmine.SpyObj<AuthService>;
  let apiErrorService: jasmine.SpyObj<ApiErrorService>;

  beforeEach(async () => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['isAuthenticated', 'login']);
    apiErrorService = jasmine.createSpyObj<ApiErrorService>('ApiErrorService', ['getFieldErrors', 'notify']);
    const notificationService = jasmine.createSpyObj<NotificationService>('NotificationService', ['success']);
    const router = jasmine.createSpyObj<Router>('Router', ['navigate']);

    authService.isAuthenticated.and.returnValue(false);
    authService.login.and.returnValue(
      throwError(() => new HttpErrorResponse({ status: 401, error: { message: 'Invalid credentials' } }))
    );
    apiErrorService.getFieldErrors.and.returnValue({ email: 'Email must be valid' });

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: ApiErrorService, useValue: apiErrorService },
        { provide: NotificationService, useValue: notificationService },
        { provide: Router, useValue: router }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should show login errors returned by the API layer', () => {
    component.form.setValue({ email: 'manager@rodojacto.com', password: 'wrong-password' });

    component.submit();
    fixture.detectChanges();

    expect(authService.login).toHaveBeenCalledWith({
      email: 'manager@rodojacto.com',
      password: 'wrong-password'
    });
    expect(apiErrorService.notify).toHaveBeenCalledWith(jasmine.any(HttpErrorResponse), 'Falha ao realizar login.');
    expect(component.fieldErrors()['email']).toBe('Email must be valid');
    expect(fixture.nativeElement.textContent).toContain('Email must be valid');
  });
});
