import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';

import { AccessLevel } from '../../core/models/access-level.model';
import { Collaborator } from '../../core/models/collaborator.model';
import { Organization } from '../../core/models/organization.model';
import { ApiErrorService } from '../../core/services/api-error.service';
import { AuthService } from '../../core/services/auth.service';
import { CollaboratorService } from '../../core/services/collaborator.service';
import { NotificationService } from '../../core/services/notification.service';
import { OrganizationService } from '../../core/services/organization.service';

@Component({
  selector: 'app-collaborators',
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './collaborators.component.html',
  styleUrl: './collaborators.component.scss'
})
export class CollaboratorsComponent {
  private readonly collaboratorService = inject(CollaboratorService);
  private readonly organizationService = inject(OrganizationService);
  private readonly authService = inject(AuthService);
  private readonly notificationService = inject(NotificationService);
  private readonly apiErrorService = inject(ApiErrorService);
  private readonly formBuilder = inject(FormBuilder);

  readonly collaborators = signal<Collaborator[]>([]);
  readonly organizations = signal<Organization[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly fieldErrors = signal<Record<string, string>>({});
  readonly editingId = signal<number | null>(null);
  readonly canManage = computed(() => this.authService.isManager());
  readonly accessLevels: AccessLevel[] = ['MANAGER', 'OPERATOR'];

  readonly form = this.formBuilder.nonNullable.group({
    fullName: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    accessLevel: ['OPERATOR' as AccessLevel, [Validators.required]],
    organizationId: [0, [Validators.required, Validators.min(1)]]
  });

  constructor() {
    this.loadData();
  }

  loadData(): void {
    this.loading.set(true);
    this.collaboratorService.findAll().pipe(
      finalize(() => this.loading.set(false))
    ).subscribe({
      next: (collaborators) => this.collaborators.set(collaborators),
      error: (error) => this.apiErrorService.notify(error, 'Falha ao carregar colaboradores.')
    });

    this.organizationService.findAll().subscribe({
      next: (organizations) => {
        this.organizations.set(organizations);
        if (!this.form.controls.organizationId.value && organizations.length > 0) {
          this.form.controls.organizationId.setValue(organizations[0].id);
        }
      },
      error: (error) => this.apiErrorService.notify(error, 'Falha ao carregar organizações.')
    });
  }

  edit(collaborator: Collaborator): void {
    if (!this.canManage()) {
      return;
    }

    this.editingId.set(collaborator.id);
    this.fieldErrors.set({});
    this.form.setValue({
      fullName: collaborator.fullName,
      email: collaborator.email,
      password: '',
      accessLevel: collaborator.accessLevel,
      organizationId: collaborator.organizationId
    });
  }

  resetForm(): void {
    this.editingId.set(null);
    this.fieldErrors.set({});
    this.form.reset({
      fullName: '',
      email: '',
      password: '',
      accessLevel: 'OPERATOR',
      organizationId: this.organizations()[0]?.id ?? 0
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
      ? this.collaboratorService.update(this.editingId()!, this.form.getRawValue())
      : this.collaboratorService.create(this.form.getRawValue());

    request.subscribe({
      next: () => {
        this.notificationService.success(
          this.editingId() ? 'Colaborador atualizado com sucesso.' : 'Colaborador criado com sucesso.'
        );
        this.resetForm();
        this.loadData();
      },
      error: (error) => {
        this.fieldErrors.set(this.apiErrorService.getFieldErrors(error));
        this.apiErrorService.notify(error, 'Falha ao salvar colaborador.');
        this.saving.set(false);
      },
      complete: () => this.saving.set(false)
    });
  }

  remove(collaborator: Collaborator): void {
    if (!this.canManage() || !confirm(`Excluir ${collaborator.fullName}?`)) {
      return;
    }

    this.collaboratorService.delete(collaborator.id).subscribe({
      next: () => {
        this.notificationService.success('Colaborador removido com sucesso.');
        this.loadData();
      },
      error: (error) => this.apiErrorService.notify(error, 'Falha ao remover colaborador.')
    });
  }

  organizationName(organizationId: number): string {
    return this.organizations().find((organization) => organization.id === organizationId)?.corporateName ?? `Org ${organizationId}`;
  }
}
