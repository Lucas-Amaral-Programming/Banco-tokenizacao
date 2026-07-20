import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { ContaService } from '../services/conta.service';

export const loginGuard: CanActivateFn = () => {
  const contaService = inject(ContaService);
  const router = inject(Router);

  if (contaService.estaLogado()) {
    return true;
  }

  return router.createUrlTree(['/login']);
};
