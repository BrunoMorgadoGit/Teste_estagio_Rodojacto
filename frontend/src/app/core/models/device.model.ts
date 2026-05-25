export interface Device {
  id: number;
  model: string;
  assetTag: string;
  organizationId: number;
  createdAt: string;
}

export interface DevicePayload {
  model: string;
  assetTag: string;
  organizationId: number;
}
