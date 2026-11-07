import { ApplicationConfig, ErrorHandler, inject, provideAppInitializer, provideZoneChangeDetection } from '@angular/core';
import { provideRouter, Router } from '@angular/router';

import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { GlobalErrorHandlerService } from './core/services/global-error-handler.service';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { DialogService } from './core/services/dialog.service';
import { AuthStateService } from './core/services/auth-state.service';
import { firstValueFrom } from 'rxjs';

export const appConfig: ApplicationConfig = {
  providers: [provideZoneChangeDetection({ eventCoalescing: true }),
  provideRouter(routes), provideHttpClient(withInterceptors([authInterceptor])),
  {
    provide: ErrorHandler,
    useClass: GlobalErrorHandlerService
  },
  provideAppInitializer(() => {
    inject(DialogService)
    const authStateService = inject(AuthStateService)
    return firstValueFrom(
      authStateService.fetchCurrentUser()
    )
  })
  ]
};
