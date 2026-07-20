import { Routes } from '@angular/router';
import { Login } from './components/login/login';
import { Cadastro } from './components/cadastro/cadastro';
import { Transacoes } from './components/transacoes/transacoes';
import { FormularioTransacao } from './components/transacoes/formulario/formulario-transacao';
import { Extrato } from './components/transacoes/extrato/extrato';
import { Home } from './components/home/home';
import { loginGuard } from './guards/login.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'cadastro', component: Cadastro },
  { path: 'home', component: Home, canActivate: [loginGuard] },
  {
    path: 'transacoes',
    component: Transacoes,
    canActivate: [loginGuard],
    children: [
      { path: '', redirectTo: 'pix', pathMatch: 'full' },
      { path: 'extrato', component: Extrato },
      { path: ':tipo', component: FormularioTransacao }
    ]
  }
];
