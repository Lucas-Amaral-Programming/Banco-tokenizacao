import { Routes } from '@angular/router';
import { Login } from './components/login/login';
import { Cadastro } from './components/cadastro/cadastro';
import { FormularioTransacao } from './components/transacoes/formulario/formulario-transacao';
import { Extrato } from './components/transacoes/extrato/extrato';
import { ExtratoPix } from './components/transacoes/extrato-pix/extrato-pix';
import { MinhasChaves } from './components/transacoes/minhas-chaves/minhas-chaves';
import { MeusLimites } from './components/transacoes/meus-limites/meus-limites';
import { Home } from './components/home/home';
import { loginGuard } from './guards/login.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'cadastro', component: Cadastro },
  { path: 'home', component: Home, canActivate: [loginGuard] },
  { path: 'extrato', component: Extrato, canActivate: [loginGuard] },
  { path: 'pix', component: FormularioTransacao, canActivate: [loginGuard], data: { tipo: 'PIX' } },
  { path: 'pix/chaves', component: MinhasChaves, canActivate: [loginGuard] },
  { path: 'pix/limites', component: MeusLimites, canActivate: [loginGuard] },
  { path: 'pix/extrato', component: ExtratoPix, canActivate: [loginGuard] },
  { path: 'deposito', component: FormularioTransacao, canActivate: [loginGuard], data: { tipo: 'DEPOSITO' } },
  { path: 'saque', component: FormularioTransacao, canActivate: [loginGuard], data: { tipo: 'SAQUE' } }
];
