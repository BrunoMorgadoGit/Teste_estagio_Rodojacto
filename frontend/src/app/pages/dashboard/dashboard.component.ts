import { Component, computed, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { finalize } from 'rxjs';

import { DashboardSummary } from '../../core/models/dashboard.model';
import { ApiErrorService } from '../../core/services/api-error.service';
import { AuthService } from '../../core/services/auth.service';
import { DashboardService } from '../../core/services/dashboard.service';

@Component({
  selector: 'app-dashboard',
  imports: [DecimalPipe],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {
  private readonly dashboardService = inject(DashboardService);
  private readonly authService = inject(AuthService);
  private readonly apiErrorService = inject(ApiErrorService);

  readonly summary = signal<DashboardSummary | null>(null);
  readonly loading = signal(true);

  readonly isManager = computed(() => this.authService.isManager());
  readonly currentUser = computed(() => this.authService.currentUser());

  constructor() {
    this.loadSummary();
  }

  private loadSummary(): void {
    this.dashboardService.getSummary().pipe(
      finalize(() => this.loading.set(false))
    ).subscribe({
      next: (summary) => this.summary.set(summary),
      error: (error) => this.apiErrorService.notify(error, 'Falha ao carregar o dashboard.')
    });
  }
}
