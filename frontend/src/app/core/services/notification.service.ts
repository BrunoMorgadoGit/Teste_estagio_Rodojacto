import { Injectable, signal } from '@angular/core';

export type NotificationType = 'success' | 'error' | 'info' | 'warning';

export interface NotificationState {
  type: NotificationType;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  readonly notification = signal<NotificationState | null>(null);

  success(message: string): void {
    this.notification.set({ type: 'success', message });
  }

  error(message: string): void {
    this.notification.set({ type: 'error', message });
  }

  info(message: string): void {
    this.notification.set({ type: 'info', message });
  }

  warning(message: string): void {
    this.notification.set({ type: 'warning', message });
  }

  clear(): void {
    this.notification.set(null);
  }
}
