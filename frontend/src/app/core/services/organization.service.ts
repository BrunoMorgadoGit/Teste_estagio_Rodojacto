import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Organization, OrganizationPayload } from '../models/organization.model';

@Injectable({
  providedIn: 'root'
})
export class OrganizationService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/organizations`;

  findAll(): Observable<Organization[]> {
    return this.http.get<Organization[]>(this.apiUrl);
  }

  findById(id: number): Observable<Organization> {
    return this.http.get<Organization>(`${this.apiUrl}/${id}`);
  }

  create(payload: OrganizationPayload): Observable<Organization> {
    return this.http.post<Organization>(this.apiUrl, payload);
  }

  update(id: number, payload: OrganizationPayload): Observable<Organization> {
    return this.http.put<Organization>(`${this.apiUrl}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
