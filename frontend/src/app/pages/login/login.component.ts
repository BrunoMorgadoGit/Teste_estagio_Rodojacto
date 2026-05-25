import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { ApiErrorService } from '../../core/services/api-error.service';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly apiErrorService = inject(ApiErrorService);
  private readonly notificationService = inject(NotificationService);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly fieldErrors = signal<Record<string, string>>({});

  readonly form = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  constructor() {
    if (this.authService.isAuthenticated()) {
      void this.router.navigate(['/dashboard']);
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.fieldErrors.set({});

    this.authService.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.notificationService.success('Login realizado com sucesso.');
        void this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        this.fieldErrors.set(this.apiErrorService.getFieldErrors(error));
        this.apiErrorService.notify(error, 'Falha ao realizar login.');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false)
    });
  }
}
