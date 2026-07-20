import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { LoginContaRequest } from '../../models/conta.model';
import { ContaService } from '../../services/conta.service';
import { CampoFormulario } from '../shared/campo-formulario/campo-formulario';
import { LayoutAuth } from '../shared/layout-auth/layout-auth';

@Component({
  selector: 'app-login',
  imports: [RouterLink, LayoutAuth, CampoFormulario],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {
  private readonly contaService = inject(ContaService);
  private readonly router = inject(Router);

  protected credenciais: LoginContaRequest = {
    cpf: '',
    senha: ''
  };

  protected readonly erro = signal<string | null>(null);
  protected readonly carregando = signal(false);

  entrar(): void {
    this.erro.set(null);
    this.carregando.set(true);

    this.contaService.login(this.credenciais).subscribe({
      next: () => {
        this.carregando.set(false);
        this.router.navigate(['/home']);
      },
      error: (falha) => {
        this.erro.set(falha?.error?.mensagem ?? 'Nao foi possivel entrar.');
        this.carregando.set(false);
      }
    });
  }
}
