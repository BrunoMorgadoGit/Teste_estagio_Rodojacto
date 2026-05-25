import { AccessLevel } from './access-level.model';

export interface AuthRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
}

export interface CurrentUser {
  id: number;
  fullName: string;
  email: string;
  accessLevel: AccessLevel;
  organizationId: number;
  organizationName: string;
  createdAt: string;
}
