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

  private dismissTimer: ReturnType<typeof setTimeout> | null = null;

  success(message: string): void {
    this.show({ type: 'success', message }, 4000);
  }

  error(message: string): void {
    this.show({ type: 'error', message }, 6000);
  }

  info(message: string): void {
    this.show({ type: 'info', message }, 4000);
  }

  warning(message: string): void {
    this.show({ type: 'warning', message }, 6000);
  }

  clear(): void {
    this.clearTimer();
    this.notification.set(null);
  }

  private show(state: NotificationState, durationMs: number): void {
    this.clearTimer();
    this.notification.set(state);
    this.dismissTimer = setTimeout(() => this.notification.set(null), durationMs);
  }

  private clearTimer(): void {
    if (this.dismissTimer !== null) {
      clearTimeout(this.dismissTimer);
      this.dismissTimer = null;
    }
  }
}
