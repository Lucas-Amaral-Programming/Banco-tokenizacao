import { Component, computed, inject, input, signal } from '@angular/core';
import { Location } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TipoChavePix, TipoTransacao, TransacaoRequest } from '../../../models/transacao-request.model';
import { TransacaoResponse } from '../../../models/transacao-response.model';
import { TransacaoService } from '../../../services/transacao.service';
import { ContaService } from '../../../services/conta.service';
import { Icone, NomeIcone } from '../../shared/icone/icone';
import { LayoutBanco } from '../../shared/layout-banco/layout-banco';
import { BottomNav } from '../../home/bottom-nav/bottom-nav';
import { ContaOrigem } from '../conta-origem/conta-origem';
import { cpfEhValido } from '../../../utils/cpf';
import { emailEhValido } from '../../../utils/email';
import { telefoneEhValido } from '../../../utils/telefone';

const TIPOS_VALIDOS: TipoTransacao[] = ['PIX', 'DEPOSITO', 'SAQUE'];

type AbaPix = 'CHAVE' | 'QR' | 'COPIA';
type EstadoChavePix = 'INDEFINIDA' | TipoChavePix | 'AMBIGUA' | 'INVALIDA';

@Component({
  selector: 'app-formulario-transacao',
  imports: [FormsModule, Icone, BottomNav, ContaOrigem, LayoutBanco],
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
  protected readonly estadoChave = signal<EstadoChavePix>('INDEFINIDA');
  protected readonly consultandoDestinatario = signal(false);
  protected readonly destinatarioResolvido = signal(false);
  protected readonly erroChave = signal<string | null>(null);
  protected nomeDestinatario = '';
  private versaoConsultaDestinatario = 0;

  protected readonly tipoChave = computed<TipoChavePix | null>(() => {
    const estado = this.estadoChave();
    return estado === 'CPF' || estado === 'EMAIL' || estado === 'CELULAR' ? estado : null;
  });

  protected readonly iconeChave = computed<NomeIcone>(() => {
    switch (this.estadoChave()) {
      case 'CPF':
        return 'conta';
      case 'EMAIL':
        return 'envelope';
      case 'CELULAR':
        return 'telefone';
      default:
        return 'pix';
    }
  });

  protected readonly placeholderChave = computed(() => {
    switch (this.estadoChave()) {
      case 'CPF':
        return '000.000.000-00';
      case 'EMAIL':
        return 'nome@email.com';
      case 'CELULAR':
        return '(11) 99999-1234';
      default:
        return 'CPF, e-mail ou celular';
    }
  });

  // numeroContaDestino guarda a chave normalizada (so digitos p/ CPF e celular,
  // trim p/ e-mail): e o que vai para resolucao, envio e idempotencia.
  // chaveExibida guarda a versao mascarada, usada apenas no input.
  protected numeroContaDestino = '';
  protected chaveExibida = '';
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

  selecionarTipoChave(tipo: 'CPF' | 'CELULAR'): void {
    if (this.estadoChave() !== 'AMBIGUA') {
      return;
    }
    this.estadoChave.set(tipo);
    this.chaveExibida = tipo === 'CPF'
      ? this.mascaraCpf(this.numeroContaDestino)
      : this.mascaraTelefone(this.numeroContaDestino);
    this.resolverDestinatario(tipo);
  }

  aoDigitarChave(evento: Event): void {
    const alvo = evento.target as HTMLInputElement;
    const digitado = alvo.value.slice(0, 120);
    this.invalidarDestinatario();

    if (/[A-Za-z@]/.test(digitado)) {
      this.estadoChave.set('EMAIL');
      this.chaveExibida = digitado;
      this.numeroContaDestino = digitado.trim().toLowerCase();
      if (emailEhValido(this.numeroContaDestino)) {
        this.resolverDestinatario('EMAIL');
      }
    } else {
      const digitos = digitado.replace(/\D/g, '').slice(0, 11);
      this.numeroContaDestino = digitos;
      this.classificarChaveNumerica(digitos);
    }
    alvo.value = this.chaveExibida;
  }

  private classificarChaveNumerica(digitos: string): void {
    if (digitos.length < 11) {
      this.estadoChave.set('INDEFINIDA');
      this.chaveExibida = digitos;
      return;
    }

    const ehCpf = cpfEhValido(digitos);
    const ehCelular = telefoneEhValido(digitos);
    if (ehCpf && ehCelular) {
      this.estadoChave.set('AMBIGUA');
      this.chaveExibida = digitos;
    } else if (ehCpf) {
      this.estadoChave.set('CPF');
      this.chaveExibida = this.mascaraCpf(digitos);
      this.resolverDestinatario('CPF');
    } else if (ehCelular) {
      this.estadoChave.set('CELULAR');
      this.chaveExibida = this.mascaraTelefone(digitos);
      this.resolverDestinatario('CELULAR');
    } else {
      this.estadoChave.set('INVALIDA');
      this.chaveExibida = digitos;
      this.erroChave.set('Chave PIX invalida.');
    }
  }

  private resolverDestinatario(tipo: TipoChavePix): void {
    const versao = ++this.versaoConsultaDestinatario;
    this.consultandoDestinatario.set(true);
    this.erroChave.set(null);
    this.transacaoService.resolverChavePix({
      tipoChavePix: tipo,
      chave: this.numeroContaDestino
    }).subscribe({
      next: (destinatario) => {
        if (versao !== this.versaoConsultaDestinatario) {
          return;
        }
        this.nomeDestinatario = destinatario.nomeTitular;
        this.destinatarioResolvido.set(true);
        this.consultandoDestinatario.set(false);
      },
      error: (falha) => {
        if (versao !== this.versaoConsultaDestinatario) {
          return;
        }
        this.nomeDestinatario = '';
        this.destinatarioResolvido.set(false);
        this.consultandoDestinatario.set(false);
        this.erroChave.set(falha?.error?.mensagem ?? 'Chave PIX nao encontrada.');
      }
    });
  }

  private invalidarDestinatario(): void {
    this.versaoConsultaDestinatario++;
    this.nomeDestinatario = '';
    this.destinatarioResolvido.set(false);
    this.consultandoDestinatario.set(false);
    this.erroChave.set(null);
  }

  protected podeEnviar(): boolean {
    if (this.carregando() || this.paraNumero(this.valorTransacaoTexto) <= 0) {
      return false;
    }
    return this.tipoTransacao() !== 'PIX'
      || (this.tipoChave() !== null
        && this.destinatarioResolvido()
        && !this.consultandoDestinatario());
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
    if (!this.podeEnviar()) {
      this.erro.set('Preencha os dados obrigatorios antes de continuar.');
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
    this.chaveExibida = '';
    this.estadoChave.set('INDEFINIDA');
    this.valorTransacaoTexto = 'R$ 0,00';
    this.descricao = '';
    this.invalidarDestinatario();
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
