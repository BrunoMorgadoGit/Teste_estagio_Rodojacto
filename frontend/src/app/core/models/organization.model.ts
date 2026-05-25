export interface Organization {
  id: number;
  corporateName: string;
  registrationCode: string;
  createdAt: string;
}

export interface OrganizationPayload {
  corporateName: string;
  registrationCode: string;
}
