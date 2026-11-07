import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { StorageService } from '../services/storage.service';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router)
  const storageService = inject(StorageService)
  const jwt = storageService.getToken();

  req = req.clone({
    headers: req.headers.set('Authorization', 'Bearer ' + jwt)
  })

  return next(req).pipe(
    catchError(err => {
      const excludedPaths = ['/auth', '/auth/login', '/auth/register', '/feed', '/'];
      if (err.status === 401 && !excludedPaths.includes(location.pathname)) {
        storageService.clearAuth()
        router.navigateByUrl('/auth/login')
      }

      return throwError(() => err)
    })
  );
};
