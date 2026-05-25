import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';

import { Device } from '../../core/models/device.model';
import { Organization } from '../../core/models/organization.model';
import { ApiErrorService } from '../../core/services/api-error.service';
import { AuthService } from '../../core/services/auth.service';
import { DeviceService } from '../../core/services/device.service';
import { NotificationService } from '../../core/services/notification.service';
import { OrganizationService } from '../../core/services/organization.service';

@Component({
  selector: 'app-devices',
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './devices.component.html',
  styleUrl: './devices.component.scss'
})
export class DevicesComponent {
  private readonly deviceService = inject(DeviceService);
  private readonly organizationService = inject(OrganizationService);
  private readonly authService = inject(AuthService);
  private readonly notificationService = inject(NotificationService);
  private readonly apiErrorService = inject(ApiErrorService);
  private readonly formBuilder = inject(FormBuilder);

  readonly devices = signal<Device[]>([]);
  readonly organizations = signal<Organization[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly fieldErrors = signal<Record<string, string>>({});
  readonly editingId = signal<number | null>(null);
  readonly canManage = computed(() => this.authService.isManager());
  readonly currentUser = computed(() => this.authService.currentUser());

  readonly form = this.formBuilder.nonNullable.group({
    model: ['', [Validators.required]],
    assetTag: ['', [Validators.required]],
    organizationId: [0, [Validators.required, Validators.min(1)]]
  });

  constructor() {
    this.loadData();
  }

  loadData(): void {
    this.loading.set(true);
    this.deviceService.findAll().pipe(
      finalize(() => this.loading.set(false))
    ).subscribe({
      next: (devices) => this.devices.set(devices),
      error: (error) => this.apiErrorService.notify(error, 'Falha ao carregar dispositivos.')
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

  edit(device: Device): void {
    if (!this.canManage()) {
      return;
    }

    this.editingId.set(device.id);
    this.fieldErrors.set({});
    this.form.setValue({
      model: device.model,
      assetTag: device.assetTag,
      organizationId: device.organizationId
    });
  }

  resetForm(): void {
    this.editingId.set(null);
    this.fieldErrors.set({});
    this.form.reset({
      model: '',
      assetTag: '',
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
      ? this.deviceService.update(this.editingId()!, this.form.getRawValue())
      : this.deviceService.create(this.form.getRawValue());

    request.subscribe({
      next: () => {
        this.notificationService.success(
          this.editingId() ? 'Dispositivo atualizado com sucesso.' : 'Dispositivo criado com sucesso.'
        );
        this.resetForm();
        this.loadData();
      },
      error: (error) => {
        this.fieldErrors.set(this.apiErrorService.getFieldErrors(error));
        this.apiErrorService.notify(error, 'Falha ao salvar dispositivo.');
        this.saving.set(false);
      },
      complete: () => this.saving.set(false)
    });
  }

  remove(device: Device): void {
    if (!this.canManage() || !confirm(`Excluir ${device.model}?`)) {
      return;
    }

    this.deviceService.delete(device.id).subscribe({
      next: () => {
        this.notificationService.success('Dispositivo removido com sucesso.');
        this.loadData();
      },
      error: (error) => this.apiErrorService.notify(error, 'Falha ao remover dispositivo.')
    });
  }

  organizationName(organizationId: number): string {
    return this.organizations().find((organization) => organization.id === organizationId)?.corporateName ?? `Org ${organizationId}`;
  }
}
