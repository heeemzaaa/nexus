import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStateService } from '../services/auth-state.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authStateService = inject(AuthStateService)
  const router = inject(Router)

  if (!authStateService.isAuthenticated()) {
    router.navigate(['/auth', 'login'])
    return false;
  }

  return true;
};
