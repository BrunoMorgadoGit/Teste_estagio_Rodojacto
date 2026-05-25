import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { of } from 'rxjs';

import { CurrentUser } from '../../core/models/auth.model';
import { DashboardSummary } from '../../core/models/dashboard.model';
import { ApiErrorService } from '../../core/services/api-error.service';
import { AuthService } from '../../core/services/auth.service';
import { DashboardService } from '../../core/services/dashboard.service';
import { DashboardComponent } from './dashboard.component';

describe('DashboardComponent', () => {
  let fixture: ComponentFixture<DashboardComponent>;

  const manager: CurrentUser = {
    id: 1,
    fullName: 'Manager',
    email: 'manager@rodojacto.com',
    accessLevel: 'MANAGER',
    organizationId: 1,
    organizationName: 'Rodojacto Matriz',
    createdAt: '2026-01-01T10:00:00'
  };

  const summary: DashboardSummary = {
    totalOrganizations: 2,
    totalCollaborators: 3,
    totalDevices: 4,
    totalManagers: 1,
    totalOperators: 2,
    devicesByOrganization: [{ organizationId: 1, organizationName: 'Rodojacto Matriz', total: 4 }],
    collaboratorsByOrganization: [{ organizationId: 1, organizationName: 'Rodojacto Matriz', total: 3 }]
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        { provide: DashboardService, useValue: { getSummary: () => of(summary) } },
        { provide: AuthService, useValue: { isManager: () => true, currentUser: signal(manager) } },
        { provide: ApiErrorService, useValue: { notify: jasmine.createSpy('notify') } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    fixture.detectChanges();
  });

  it('should render dashboard cards and global manager metrics', () => {
    const text = fixture.nativeElement.textContent;

    expect(text).toContain('Organizações');
    expect(text).toContain('Colaboradores');
    expect(text).toContain('Dispositivos');
    expect(text).toContain('Dispositivos por Organização');
    expect(text).toContain('Colaboradores por Organização');
    expect(text).toContain('Rodojacto Matriz');
  });
});
