import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { of } from 'rxjs';

import { CurrentUser } from '../core/models/auth.model';
import { Organization } from '../core/models/organization.model';
import { ApiErrorService } from '../core/services/api-error.service';
import { AuthService } from '../core/services/auth.service';
import { CollaboratorService } from '../core/services/collaborator.service';
import { DeviceService } from '../core/services/device.service';
import { NotificationService } from '../core/services/notification.service';
import { OrganizationService } from '../core/services/organization.service';
import { CollaboratorsComponent } from './collaborators/collaborators.component';
import { DevicesComponent } from './devices/devices.component';
import { OrganizationsComponent } from './organizations/organizations.component';

describe('CRUD components', () => {
  const organization: Organization = {
    id: 1,
    corporateName: 'Rodojacto Matriz',
    registrationCode: 'RODOJACTO-001',
    createdAt: '2026-01-01T10:00:00'
  };

  const operator: CurrentUser = {
    id: 2,
    fullName: 'Operator',
    email: 'operator@rodojacto.com',
    accessLevel: 'OPERATOR',
    organizationId: 1,
    organizationName: 'Rodojacto Matriz',
    createdAt: '2026-01-01T10:00:00'
  };

  const apiErrorService = {
    notify: jasmine.createSpy('notify'),
    getFieldErrors: jasmine.createSpy('getFieldErrors').and.returnValue({})
  };

  const notificationService = {
    success: jasmine.createSpy('success')
  };

  afterEach(() => {
    TestBed.resetTestingModule();
  });

  it('should list organizations and show write actions for manager', async () => {
    await TestBed.configureTestingModule({
      imports: [OrganizationsComponent],
      providers: [
        { provide: OrganizationService, useValue: { findAll: () => of([organization]) } },
        { provide: AuthService, useValue: { isManager: () => true } },
        { provide: ApiErrorService, useValue: apiErrorService },
        { provide: NotificationService, useValue: notificationService }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(OrganizationsComponent);
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Rodojacto Matriz');
    expect(text).toContain('Cadastrar');
    expect(text).toContain('Editar');
    expect(text).toContain('Excluir');
  });

  it('should hide organization write actions for operator', async () => {
    await TestBed.configureTestingModule({
      imports: [OrganizationsComponent],
      providers: [
        { provide: OrganizationService, useValue: { findAll: () => of([organization]) } },
        { provide: AuthService, useValue: { isManager: () => false } },
        { provide: ApiErrorService, useValue: apiErrorService },
        { provide: NotificationService, useValue: notificationService }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(OrganizationsComponent);
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Rodojacto Matriz');
    expect(text).toContain('Acesso somente leitura');
    expect(text).not.toContain('Cadastrar');
    expect(text).not.toContain('Editar');
    expect(text).not.toContain('Excluir');
  });

  it('should list collaborators and hide write actions for operator', async () => {
    await TestBed.configureTestingModule({
      imports: [CollaboratorsComponent],
      providers: [
        {
          provide: CollaboratorService,
          useValue: {
            findAll: () =>
              of([
                {
                  id: 3,
                  fullName: 'Operator',
                  email: 'operator@rodojacto.com',
                  accessLevel: 'OPERATOR',
                  organizationId: 1,
                  createdAt: '2026-01-01T10:00:00'
                }
              ])
          }
        },
        { provide: OrganizationService, useValue: { findAll: () => of([organization]) } },
        { provide: AuthService, useValue: { isManager: () => false } },
        { provide: ApiErrorService, useValue: apiErrorService },
        { provide: NotificationService, useValue: notificationService }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(CollaboratorsComponent);
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('operator@rodojacto.com');
    expect(text).toContain('Acesso somente leitura');
    expect(text).not.toContain('Cadastrar');
    expect(text).not.toContain('Editar');
    expect(text).not.toContain('Excluir');
  });

  it('should list devices and hide write actions for operator', async () => {
    await TestBed.configureTestingModule({
      imports: [DevicesComponent],
      providers: [
        {
          provide: DeviceService,
          useValue: {
            findAll: () =>
              of([
                {
                  id: 4,
                  model: 'Notebook Dell',
                  assetTag: 'NOTE-001',
                  organizationId: 1,
                  createdAt: '2026-01-01T10:00:00'
                }
              ])
          }
        },
        { provide: OrganizationService, useValue: { findAll: () => of([organization]) } },
        { provide: AuthService, useValue: { isManager: () => false, currentUser: signal(operator) } },
        { provide: ApiErrorService, useValue: apiErrorService },
        { provide: NotificationService, useValue: notificationService }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(DevicesComponent);
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Notebook Dell');
    expect(text).toContain('NOTE-001');
    expect(text).toContain('Acesso somente leitura');
    expect(text).not.toContain('Cadastrar');
    expect(text).not.toContain('Editar');
    expect(text).not.toContain('Excluir');
  });
});
