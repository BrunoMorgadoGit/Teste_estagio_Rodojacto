import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Device, DevicePayload } from '../models/device.model';

@Injectable({
  providedIn: 'root'
})
export class DeviceService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/devices`;

  findAll(): Observable<Device[]> {
    return this.http.get<Device[]>(this.apiUrl);
  }

  findById(id: number): Observable<Device> {
    return this.http.get<Device>(`${this.apiUrl}/${id}`);
  }

  create(payload: DevicePayload): Observable<Device> {
    return this.http.post<Device>(this.apiUrl, payload);
  }

  update(id: number, payload: DevicePayload): Observable<Device> {
    return this.http.put<Device>(`${this.apiUrl}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
