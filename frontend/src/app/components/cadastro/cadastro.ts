import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CadastroContaRequest, ContaResponse, TipoConta } from '../../models/conta.model';
import { ContaService } from '../../services/conta.service';
import { cpfEhValido } from '../../utils/cpf';
import { emailEhValido } from '../../utils/email';
import { nomeEhCompleto } from '../../utils/nome';
import { telefoneEhValido } from '../../utils/telefone';
import { CampoFormulario } from '../shared/campo-formulario/campo-formulario';
import { LayoutAuth } from '../shared/layout-auth/layout-auth';

type CampoErro = 'nomeTitular' | 'cpf' | 'telefone' | 'email' | 'senha';

const SEM_ERROS: Record<CampoErro, string | null> = {
  nomeTitular: null,
  cpf: null,
  telefone: null,
  email: null,
  senha: null
};

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
    telefone: '',
    email: '',
    tipoConta: 'CORRENTE',
    senha: ''
  };

  protected readonly contaCriada = signal<ContaResponse | null>(null);
  protected readonly erros = signal<Record<CampoErro, string | null>>({ ...SEM_ERROS });
  protected readonly erroGeral = signal<string | null>(null);
  protected readonly carregando = signal(false);

  cadastrar(): void {
    this.erros.set({ ...SEM_ERROS });
    this.erroGeral.set(null);

    const erros = { ...SEM_ERROS };

    if (!this.dados.nomeTitular.trim()) {
      erros.nomeTitular = 'Informe o nome do titular.';
    } else if (!nomeEhCompleto(this.dados.nomeTitular)) {
      erros.nomeTitular = 'Informe o nome completo (nome e sobrenome).';
    }
    if (!this.dados.cpf.trim()) {
      erros.cpf = 'Informe o CPF.';
    } else if (!cpfEhValido(this.dados.cpf)) {
      erros.cpf = 'CPF invalido.';
    }
    if (!this.dados.telefone.trim()) {
      erros.telefone = 'Informe o celular.';
    } else if (!telefoneEhValido(this.dados.telefone)) {
      erros.telefone = 'Celular invalido.';
    }
    if (!this.dados.email.trim()) {
      erros.email = 'Informe o e-mail.';
    } else if (!emailEhValido(this.dados.email)) {
      erros.email = 'E-mail invalido.';
    }
    if (!this.dados.senha.trim()) {
      erros.senha = 'Informe a senha.';
    }

    if (Object.values(erros).some((mensagem) => mensagem !== null)) {
      this.erros.set(erros);
      return;
    }

    this.carregando.set(true);

    this.contaService.cadastrar(this.dados).subscribe({
      next: (conta) => {
        this.carregando.set(false);
        this.contaCriada.set(conta);
      },
      error: (falha) => {
        this.exibirErroDaApi(falha);
        this.carregando.set(false);
      }
    });
  }

  private exibirErroDaApi(falha: any): void {
    const campo = falha?.error?.campo as CampoErro | undefined;
    const mensagem = falha?.error?.mensagem ?? 'Nao foi possivel cadastrar a conta.';

    if (campo && campo in SEM_ERROS) {
      this.erros.set({ ...SEM_ERROS, [campo]: mensagem });
    } else {
      this.erroGeral.set(mensagem);
    }
  }
}
