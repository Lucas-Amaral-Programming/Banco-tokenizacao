import { Component, input } from '@angular/core';
import { Beneficios } from '../beneficios/beneficios';
import { Logo } from '../logo/logo';

export type VarianteLayoutAuth =
  | 'padrao'
  | 'login-destaque'
  | 'cadastro-destaque';

@Component({
  selector: 'app-layout-auth',
  imports: [Logo, Beneficios],
  templateUrl: './layout-auth.html',
  styleUrl: './layout-auth.scss'
})
export class LayoutAuth {
  readonly tituloCartao = input.required<string>();
  readonly variante = input<VarianteLayoutAuth>('padrao');
}
