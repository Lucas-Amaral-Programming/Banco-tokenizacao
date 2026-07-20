import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { ContaService } from '../services/conta.service';

export const loginGuard: CanActivateFn = () => {
  const contaService = inject(ContaService);
  const router = inject(Router);

  if (contaService.estaLogado()) {
    return true;
  }

  return contaService.carregarSessao().pipe(
    map((conta) => (conta ? true : router.createUrlTree(['/login'])))
  );
};
