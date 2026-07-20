import { Component, computed, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TipoTransacao, TransacaoRequest } from '../../../models/transacao-request.model';
import { TransacaoResponse } from '../../../models/transacao-response.model';
import { TransacaoService } from '../../../services/transacao.service';
import { ContaService } from '../../../services/conta.service';
import { CampoFormulario } from '../../shared/campo-formulario/campo-formulario';

const TIPOS_VALIDOS: TipoTransacao[] = ['PIX', 'DEPOSITO', 'SAQUE'];

@Component({
  selector: 'app-formulario-transacao',
  imports: [FormsModule, CampoFormulario],
  templateUrl: './formulario-transacao.html',
  styleUrl: './formulario-transacao.scss'
})
export class FormularioTransacao {
  private readonly transacaoService = inject(TransacaoService);
  private readonly contaService = inject(ContaService);

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

  protected numeroContaDestino = '';
  protected valorTransacaoTexto = 'R$ 0,00';
  protected descricao = '';

  protected readonly resposta = signal<TransacaoResponse | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly carregando = signal(false);

  enviar(): void {
    const tipo = this.tipoTransacao();
    if (!tipo) {
      this.erro.set('Tipo de transacao invalido.');
      return;
    }

    const request: TransacaoRequest = {
      tipoTransacao: tipo,
      numeroContaOrigem: this.exigeOrigem() ? (this.contaAtual()?.numeroConta ?? '') : '',
      numeroContaDestino: this.exigeDestino() ? this.numeroContaDestino : '',
      valorTransacao: this.paraNumero(this.valorTransacaoTexto),
      descricaoTransacao: this.descricao
    };

    this.resposta.set(null);
    this.erro.set(null);
    this.carregando.set(true);

    this.transacaoService.criarTransacao(request).subscribe({
      next: (transacao) => {
        this.resposta.set(transacao);
        this.carregando.set(false);
        this.limparCampos();
      },
      error: (falha) => {
        this.erro.set(falha?.error?.mensagem ?? 'Nao foi possivel processar a transacao.');
        this.carregando.set(false);
      }
    });
  }

  private limparCampos(): void {
    this.numeroContaDestino = '';
    this.valorTransacaoTexto = 'R$ 0,00';
    this.descricao = '';
  }

  private paraNumero(valorFormatado: string): number {
    const digitos = valorFormatado.replace(/\D/g, '');
    return digitos === '' ? 0 : parseInt(digitos, 10) / 100;
  }
}
