import { Component, computed, inject, input, signal } from '@angular/core';
import { Location } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TipoChavePix, TipoTransacao, TransacaoRequest } from '../../../models/transacao-request.model';
import { TransacaoResponse } from '../../../models/transacao-response.model';
import { TransacaoService } from '../../../services/transacao.service';
import { ContaService } from '../../../services/conta.service';
import { Icone, NomeIcone } from '../../shared/icone/icone';
import { BottomNav } from '../../home/bottom-nav/bottom-nav';
import { ContaOrigem } from '../conta-origem/conta-origem';

const TIPOS_VALIDOS: TipoTransacao[] = ['PIX', 'DEPOSITO', 'SAQUE'];

type TipoChave = 'CPF' | 'EMAIL' | 'CELULAR';
type AbaPix = 'CHAVE' | 'QR' | 'COPIA';

@Component({
  selector: 'app-formulario-transacao',
  imports: [FormsModule, Icone, BottomNav, ContaOrigem],
  templateUrl: './formulario-transacao.html',
  styleUrl: './formulario-transacao.scss'
})
export class FormularioTransacao {
  private readonly transacaoService = inject(TransacaoService);
  private readonly contaService = inject(ContaService);
  private readonly location = inject(Location);

  protected readonly contaAtual = this.contaService.contaAtual;

  readonly tipo = input<string>();

  protected readonly tipoTransacao = computed<TipoTransacao | null>(() => {
    const tipoEmMaiusculas = (this.tipo() ?? '').toUpperCase();
    return TIPOS_VALIDOS.includes(tipoEmMaiusculas as TipoTransacao)
      ? (tipoEmMaiusculas as TipoTransacao)
      : null;
  });

  protected readonly exigeOrigem = computed(() => {
    const tipo = this.tipoTransacao();
    return tipo === 'SAQUE' || tipo === 'PIX';
  });

  protected readonly exigeDestino = computed(() => {
    const tipo = this.tipoTransacao();
    return tipo === 'DEPOSITO' || tipo === 'PIX';
  });

  // ----- Estado especifico da tela PIX -----
  protected readonly aba = signal<AbaPix>('CHAVE');
  protected readonly tipoChave = signal<TipoChave>('CELULAR');

  // Mock: sem endpoint de consulta de chave, o nome eh resolvido localmente ao
  // completar a chave (banco/instituicao viria junto e esta oculto por enquanto).
  protected nomeDestinatario = '';

  protected readonly iconeChave = computed<NomeIcone>(() => {
    switch (this.tipoChave()) {
      case 'CPF':
        return 'conta';
      case 'EMAIL':
        return 'envelope';
      default:
        return 'telefone';
    }
  });

  protected readonly placeholderChave = computed(() => {
    switch (this.tipoChave()) {
      case 'CPF':
        return '000.000.000-00';
      case 'EMAIL':
        return 'nome@email.com';
      default:
        return '(11) 99999-1234';
    }
  });

  protected numeroContaDestino = '';
  protected valorTransacaoTexto = 'R$ 0,00';
  protected descricao = '';

  protected readonly resposta = signal<TransacaoResponse | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly carregando = signal(false);

  private static readonly CHAVE_STORAGE = 'transacao-pendente';
  private chaveIdempotencia: string | null = null;
  private assinaturaChave: string | null = null;

  constructor() {
    const pendente = sessionStorage.getItem(FormularioTransacao.CHAVE_STORAGE);
    if (pendente) {
      try {
        const dados = JSON.parse(pendente) as { chave: string; assinatura: string };
        this.chaveIdempotencia = dados.chave;
        this.assinaturaChave = dados.assinatura;
      } catch {
        sessionStorage.removeItem(FormularioTransacao.CHAVE_STORAGE);
      }
    }
  }

  voltar(): void {
    this.location.back();
  }

  selecionarAba(aba: AbaPix): void {
    this.aba.set(aba);
  }

  selecionarTipoChave(tipo: TipoChave): void {
    this.tipoChave.set(tipo);
    this.numeroContaDestino = '';
    this.resolverDestinatario();
  }

  aoDigitarChave(evento: Event): void {
    const alvo = evento.target as HTMLInputElement;
    let valor = alvo.value;
    if (this.tipoChave() === 'CPF') {
      valor = this.mascaraCpf(valor);
    } else if (this.tipoChave() === 'CELULAR') {
      valor = this.mascaraTelefone(valor);
    }
    alvo.value = valor;
    this.numeroContaDestino = valor;
    this.resolverDestinatario();
  }

  private resolverDestinatario(): void {
    // Mock: quando a chave estiver completa, o nome seria retornado pela consulta.
    this.nomeDestinatario = this.chaveCompleta(this.numeroContaDestino) ? 'Ana Souza' : '';
  }

  private chaveCompleta(chave: string): boolean {
    if (this.tipoChave() === 'EMAIL') {
      return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(chave);
    }
    return chave.replace(/\D/g, '').length === 11;
  }

  aoDigitarValor(evento: Event): void {
    const alvo = evento.target as HTMLInputElement;
    const valor = this.mascaraMoeda(alvo.value);
    alvo.value = valor;
    this.valorTransacaoTexto = valor;
  }

  limparDescricao(): void {
    this.descricao = '';
  }

  enviar(): void {
    if (this.carregando()) {
      return;
    }

    const tipo = this.tipoTransacao();
    if (!tipo) {
      this.erro.set('Tipo de transacao invalido.');
      return;
    }

    // Deposito credita a propria conta logada.
    const destino =
      tipo === 'DEPOSITO'
        ? (this.contaAtual()?.numeroConta ?? '')
        : this.exigeDestino()
          ? this.numeroContaDestino
          : '';
    const valor = this.paraNumero(this.valorTransacaoTexto);
    const tipoChavePix: TipoChavePix | null = tipo === 'PIX' ? this.tipoChave() : null;

    const request: TransacaoRequest = {
      tipoTransacao: tipo,
      numeroContaDestino: destino,
      tipoChavePix,
      valorTransacao: valor,
      descricaoTransacao: this.descricao
    };

    const chave = this.obterChaveIdempotencia(tipo, destino, tipoChavePix, valor);

    this.resposta.set(null);
    this.erro.set(null);
    this.carregando.set(true);

    this.transacaoService.criarTransacao(request, chave).subscribe({
      next: (transacao) => {
        this.resposta.set(transacao);
        this.carregando.set(false);
        this.limparChaveIdempotencia();
        this.limparCampos();
      },
      error: (falha) => {
        this.erro.set(falha?.error?.mensagem ?? 'Nao foi possivel processar a transacao.');
        this.carregando.set(false);
      }
    });
  }

  private obterChaveIdempotencia(
    tipo: string,
    destino: string,
    tipoChavePix: TipoChavePix | null,
    valor: number
  ): string {
    const assinatura = `${tipo}|${destino}|${tipoChavePix ?? ''}|${valor}`;
    if (!this.chaveIdempotencia || this.assinaturaChave !== assinatura) {
      this.chaveIdempotencia = crypto.randomUUID();
      this.assinaturaChave = assinatura;
      sessionStorage.setItem(
        FormularioTransacao.CHAVE_STORAGE,
        JSON.stringify({ chave: this.chaveIdempotencia, assinatura })
      );
    }
    return this.chaveIdempotencia;
  }

  private limparChaveIdempotencia(): void {
    this.chaveIdempotencia = null;
    this.assinaturaChave = null;
    sessionStorage.removeItem(FormularioTransacao.CHAVE_STORAGE);
  }

  private limparCampos(): void {
    this.numeroContaDestino = '';
    this.valorTransacaoTexto = 'R$ 0,00';
    this.descricao = '';
    this.nomeDestinatario = '';
  }

  private paraNumero(valorFormatado: string): number {
    const digitos = valorFormatado.replace(/\D/g, '');
    return digitos === '' ? 0 : parseInt(digitos, 10) / 100;
  }

  private mascaraMoeda(valor: string): string {
    const digitos = valor.replace(/\D/g, '').slice(0, 13);
    const centavosTotais = digitos === '' ? 0 : parseInt(digitos, 10);
    const reais = Math.floor(centavosTotais / 100);
    const centavos = String(centavosTotais % 100).padStart(2, '0');
    return `R$ ${reais.toLocaleString('pt-BR')},${centavos}`;
  }

  private mascaraCpf(valor: string): string {
    const d = valor.replace(/\D/g, '').slice(0, 11);
    if (d.length <= 3) {
      return d;
    }
    if (d.length <= 6) {
      return `${d.slice(0, 3)}.${d.slice(3)}`;
    }
    if (d.length <= 9) {
      return `${d.slice(0, 3)}.${d.slice(3, 6)}.${d.slice(6)}`;
    }
    return `${d.slice(0, 3)}.${d.slice(3, 6)}.${d.slice(6, 9)}-${d.slice(9)}`;
  }

  private mascaraTelefone(valor: string): string {
    const d = valor.replace(/\D/g, '').slice(0, 11);
    if (d.length === 0) {
      return '';
    }
    if (d.length <= 2) {
      return `(${d}`;
    }
    if (d.length <= 7) {
      return `(${d.slice(0, 2)}) ${d.slice(2)}`;
    }
    return `(${d.slice(0, 2)}) ${d.slice(2, 7)}-${d.slice(7)}`;
  }
}
