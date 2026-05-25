import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';

import { Organization } from '../../core/models/organization.model';
import { ApiErrorService } from '../../core/services/api-error.service';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { OrganizationService } from '../../core/services/organization.service';

@Component({
  selector: 'app-organizations',
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './organizations.component.html',
  styleUrl: './organizations.component.scss'
})
export class OrganizationsComponent {
  private readonly organizationService = inject(OrganizationService);
  private readonly authService = inject(AuthService);
  private readonly notificationService = inject(NotificationService);
  private readonly apiErrorService = inject(ApiErrorService);
  private readonly formBuilder = inject(FormBuilder);

  readonly organizations = signal<Organization[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly fieldErrors = signal<Record<string, string>>({});
  readonly editingId = signal<number | null>(null);
  readonly canManage = computed(() => this.authService.isManager());

  readonly form = this.formBuilder.nonNullable.group({
    corporateName: ['', [Validators.required]],
    registrationCode: ['', [Validators.required]]
  });

  constructor() {
    this.loadOrganizations();
  }

  loadOrganizations(): void {
    this.loading.set(true);
    this.organizationService.findAll().pipe(
      finalize(() => this.loading.set(false))
    ).subscribe({
      next: (organizations) => this.organizations.set(organizations),
      error: (error) => this.apiErrorService.notify(error, 'Falha ao carregar organizações.')
    });
  }

  edit(organization: Organization): void {
    if (!this.canManage()) {
      return;
    }

    this.editingId.set(organization.id);
    this.fieldErrors.set({});
    this.form.setValue({
      corporateName: organization.corporateName,
      registrationCode: organization.registrationCode
    });
  }

  resetForm(): void {
    this.editingId.set(null);
    this.fieldErrors.set({});
    this.form.reset({
      corporateName: '',
      registrationCode: ''
    });
  }

  submit(): void {
    if (!this.canManage()) {
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.fieldErrors.set({});

    const request = this.editingId()
      ? this.organizationService.update(this.editingId()!, this.form.getRawValue())
      : this.organizationService.create(this.form.getRawValue());

    request.subscribe({
      next: () => {
        this.notificationService.success(
          this.editingId() ? 'Organização atualizada com sucesso.' : 'Organização criada com sucesso.'
        );
        this.resetForm();
        this.loadOrganizations();
      },
      error: (error) => {
        this.fieldErrors.set(this.apiErrorService.getFieldErrors(error));
        this.apiErrorService.notify(error, 'Falha ao salvar organização.');
        this.saving.set(false);
      },
      complete: () => this.saving.set(false)
    });
  }

  remove(organization: Organization): void {
    if (!this.canManage() || !confirm(`Excluir ${organization.corporateName}?`)) {
      return;
    }

    this.organizationService.delete(organization.id).subscribe({
      next: () => {
        this.notificationService.success('Organização removida com sucesso.');
        this.loadOrganizations();
      },
      error: (error) => this.apiErrorService.notify(error, 'Falha ao remover organização.')
    });
  }
}
