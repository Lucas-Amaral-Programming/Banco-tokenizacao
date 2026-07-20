import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { ContaService } from '../services/conta.service';

export const erroAutenticacaoInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const contaService = inject(ContaService);

  return next(req).pipe(
    catchError((erro: HttpErrorResponse) => {
      const ehRotaAuth = req.url.includes('/api/auth/login') || req.url.includes('/api/auth/me');
      if (erro.status === 401 && !ehRotaAuth) {
        contaService.limparSessao();
        router.navigate(['/login']);
      }
      return throwError(() => erro);
    })
  );
};
