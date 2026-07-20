import {
  ApplicationConfig,
  LOCALE_ID,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
  inject
} from '@angular/core';
import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';
import {
  provideHttpClient,
  withInterceptors,
  withXsrfConfiguration
} from '@angular/common/http';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { firstValueFrom, switchMap } from 'rxjs';

import { routes } from './app.routes';
import { erroAutenticacaoInterceptor } from './interceptors/erro-autenticacao.interceptor';
import { ContaService } from './services/conta.service';

registerLocaleData(localePt);

export const appConfig: ApplicationConfig = {
  providers: [
    { provide: LOCALE_ID, useValue: 'pt-BR' },
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(
      withXsrfConfiguration({
        cookieName: 'XSRF-TOKEN',
        headerName: 'X-XSRF-TOKEN'
      }),
      withInterceptors([erroAutenticacaoInterceptor])
    ),
    provideAppInitializer(() => {
      const contaService = inject(ContaService);
      return firstValueFrom(
        contaService.iniciarCsrf().pipe(switchMap(() => contaService.carregarSessao()))
      );
    }),
    provideRouter(routes, withComponentInputBinding())
  ]
};
