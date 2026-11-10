import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { Auth } from './auth';

// Agrega "Authorization: Bearer <token>" a cada peticion al backend.
// Si el backend responde 401 (token expirado, alterado o cuenta desactivada),
// cierra la sesion y vuelve a la pantalla de inicio de sesion.
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(Auth);
  const router = inject(Router);
  const token = auth.obtenerToken();
  const esLogin = req.url.endsWith('/api/auth/login');

  const peticion = token && !esLogin ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(peticion).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !esLogin) {
        auth.cerrarSesion();
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
