import { Component, inject, signal } from '@angular/core';
import { TransacaoResponse } from '../../../models/transacao-response.model';
import { TransacaoService } from '../../../services/transacao.service';
import { ContaService } from '../../../services/conta.service';

@Component({
  selector: 'app-extrato',
  imports: [],
  templateUrl: './extrato.html',
  styleUrl: './extrato.scss'
})
export class Extrato {
  private readonly transacaoService = inject(TransacaoService);
  private readonly contaService = inject(ContaService);

  protected readonly transacoes = signal<TransacaoResponse[]>([]);
  protected readonly carregando = signal(false);
  protected readonly erro = signal<string | null>(null);

  constructor() {
    this.carregando.set(true);
    this.transacaoService.listarExtrato().subscribe({
      next: (lista) => {
        this.transacoes.set(lista);
        this.carregando.set(false);
      },
      error: () => {
        this.erro.set('Nao foi possivel carregar o extrato.');
        this.carregando.set(false);
      }
    });
  }

  protected contraParte(transacao: TransacaoResponse): string {
    const numeroAtual = this.contaService.contaAtual()?.numeroConta;
    const enviada = transacao.numeroContaOrigem === numeroAtual;
    const outra = enviada ? transacao.numeroContaDestino : transacao.numeroContaOrigem;
    return outra ?? '-';
  }
}
