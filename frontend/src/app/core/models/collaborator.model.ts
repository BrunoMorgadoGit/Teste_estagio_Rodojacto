import { AccessLevel } from './access-level.model';

export interface Collaborator {
  id: number;
  fullName: string;
  email: string;
  accessLevel: AccessLevel;
  organizationId: number;
  createdAt: string;
}

export interface CollaboratorPayload {
  fullName: string;
  email: string;
  password: string;
  accessLevel: AccessLevel;
  organizationId: number;
}
