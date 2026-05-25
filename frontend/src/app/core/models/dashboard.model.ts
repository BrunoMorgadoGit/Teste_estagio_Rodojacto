export interface OrganizationMetric {
  organizationId: number;
  organizationName: string;
  total: number;
}

export interface DashboardSummary {
  organizationId?: number;
  organizationName?: string;
  totalOrganizations?: number;
  totalCollaborators: number;
  totalDevices: number;
  totalManagers: number;
  totalOperators: number;
  devicesByOrganization?: OrganizationMetric[];
  collaboratorsByOrganization?: OrganizationMetric[];
}
