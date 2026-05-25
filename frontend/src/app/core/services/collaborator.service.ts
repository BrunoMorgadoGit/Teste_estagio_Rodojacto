import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Collaborator, CollaboratorPayload } from '../models/collaborator.model';

@Injectable({
  providedIn: 'root'
})
export class CollaboratorService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/collaborators`;

  findAll(): Observable<Collaborator[]> {
    return this.http.get<Collaborator[]>(this.apiUrl);
  }

  findById(id: number): Observable<Collaborator> {
    return this.http.get<Collaborator>(`${this.apiUrl}/${id}`);
  }

  create(payload: CollaboratorPayload): Observable<Collaborator> {
    return this.http.post<Collaborator>(this.apiUrl, payload);
  }

  update(id: number, payload: CollaboratorPayload): Observable<Collaborator> {
    return this.http.put<Collaborator>(`${this.apiUrl}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
