import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CadastroContaRequest, ContaResponse, TipoConta } from '../../models/conta.model';
import { ContaService } from '../../services/conta.service';
import { cpfEhValido } from '../../utils/cpf';
import { emailEhValido } from '../../utils/email';
import { CampoFormulario } from '../shared/campo-formulario/campo-formulario';
import { LayoutAuth } from '../shared/layout-auth/layout-auth';

@Component({
  selector: 'app-cadastro',
  imports: [RouterLink, LayoutAuth, CampoFormulario],
  templateUrl: './cadastro.html',
  styleUrl: './cadastro.scss'
})
export class Cadastro {
  private readonly contaService = inject(ContaService);

  protected readonly tiposConta: TipoConta[] = ['CORRENTE', 'POUPANCA'];

  protected dados: CadastroContaRequest = {
    nomeTitular: '',
    cpf: '',
    email: '',
    tipoConta: 'CORRENTE',
    senha: ''
  };

  protected readonly contaCriada = signal<ContaResponse | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly carregando = signal(false);

  cadastrar(): void {
    this.erro.set(null);

    if (!cpfEhValido(this.dados.cpf)) {
      this.erro.set('CPF invalido.');
      return;
    }
    if (!emailEhValido(this.dados.email)) {
      this.erro.set('E-mail invalido.');
      return;
    }

    this.carregando.set(true);

    this.contaService.cadastrar(this.dados).subscribe({
      next: (conta) => {
        this.carregando.set(false);
        this.contaCriada.set(conta);
      },
      error: (falha) => {
        this.erro.set(falha?.error?.mensagem ?? 'Nao foi possivel cadastrar a conta.');
        this.carregando.set(false);
      }
    });
  }
}
