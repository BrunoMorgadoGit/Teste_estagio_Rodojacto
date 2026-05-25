import { Component, computed, inject } from '@angular/core';

import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-alert-banner',
  templateUrl: './alert-banner.component.html',
  styleUrl: './alert-banner.component.scss'
})
export class AlertBannerComponent {
  private readonly notificationService = inject(NotificationService);

  readonly notification = computed(() => this.notificationService.notification());

  clear(): void {
    this.notificationService.clear();
  }
}
