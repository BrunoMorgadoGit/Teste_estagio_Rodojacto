import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import { ApiError } from '../models/api-error.model';
import { NotificationService } from './notification.service';

@Injectable({
  providedIn: 'root'
})
export class ApiErrorService {
  constructor(private readonly notificationService: NotificationService) {}

  getFieldErrors(error: unknown): Record<string, string> {
    if (error instanceof HttpErrorResponse && error.error?.fields) {
      return error.error.fields as Record<string, string>;
    }

    return {};
  }

  notify(error: unknown, fallbackMessage = 'Não foi possível concluir a operação.'): void {
    this.notificationService.error(this.resolveMessage(error, fallbackMessage));
  }

  private resolveMessage(error: unknown, fallbackMessage: string): string {
    if (!(error instanceof HttpErrorResponse)) {
      return fallbackMessage;
    }

    const apiError = error.error as ApiError | undefined;

    switch (error.status) {
      case 400:
        return apiError?.message ?? 'Dados inválidos enviados para a API.';
      case 403:
        return 'Você não tem permissão para acessar este recurso.';
      case 404:
        return apiError?.message ?? 'Recurso não encontrado.';
      case 409:
        return apiError?.message ?? 'Conflito de dados.';
      case 0:
        return 'Não foi possível conectar ao backend. Verifique se a API está em execução.';
      default:
        return apiError?.message ?? fallbackMessage;
    }
  }
}
