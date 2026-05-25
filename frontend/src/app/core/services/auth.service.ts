import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, of, switchMap, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthRequest, AuthResponse, CurrentUser } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly apiUrl = `${environment.apiUrl}/auth`;
  private readonly tokenKey = 'rodojacto_token';
  private readonly currentUserState = signal<CurrentUser | null>(null);

  readonly currentUser = computed(() => this.currentUserState());
  readonly accessLevel = computed(() => this.currentUserState()?.accessLevel ?? null);

  login(payload: AuthRequest): Observable<CurrentUser> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, payload).pipe(
      tap((response) => localStorage.setItem(this.tokenKey, response.accessToken)),
      switchMap(() => this.fetchCurrentUser())
    );
  }

  fetchCurrentUser(): Observable<CurrentUser> {
    return this.http.get<CurrentUser>(`${this.apiUrl}/me`).pipe(
      tap((user) => this.currentUserState.set(user))
    );
  }

  restoreSession(): void {
    if (!this.getToken() || this.currentUserState()) {
      return;
    }

    this.fetchCurrentUser().subscribe({
      error: () => this.logout(false)
    });
  }

  logout(redirect = true): void {
    localStorage.removeItem(this.tokenKey);
    this.currentUserState.set(null);

    if (redirect) {
      void this.router.navigate(['/login']);
    }
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isAuthenticated(): boolean {
    return Boolean(this.getToken());
  }

  getCurrentUser(): Observable<CurrentUser | null> {
    if (this.currentUserState()) {
      return of(this.currentUserState());
    }

    if (!this.getToken()) {
      return of(null);
    }

    return this.fetchCurrentUser();
  }

  isManager(): boolean {
    return this.currentUserState()?.accessLevel === 'MANAGER';
  }
}
