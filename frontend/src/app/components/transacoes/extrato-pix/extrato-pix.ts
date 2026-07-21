import { Component, computed, inject, signal } from '@angular/core';
import { Location } from '@angular/common';
import { TransacaoResponse } from '../../../models/transacao-response.model';
import { TransacaoService } from '../../../services/transacao.service';
import { ContaService } from '../../../services/conta.service';
import { Icone } from '../../shared/icone/icone';
import { LayoutBanco } from '../../shared/layout-banco/layout-banco';
import { BottomNav } from '../../home/bottom-nav/bottom-nav';
import { TransacaoItem } from '../extrato/transacao-item/transacao-item';

@Component({
  selector: 'app-extrato-pix',
  imports: [Icone, LayoutBanco, BottomNav, TransacaoItem],
  templateUrl: './extrato-pix.html',
  styleUrl: './extrato-pix.scss'
})
export class ExtratoPix {
  private readonly transacaoService = inject(TransacaoService);
  private readonly contaService = inject(ContaService);
  private readonly location = inject(Location);

  protected readonly transacoes = signal<TransacaoResponse[]>([]);
  protected readonly carregando = signal(false);
  protected readonly erro = signal<string | null>(null);

  protected readonly numeroContaAtual = computed(
    () => this.contaService.contaAtual()?.numeroConta ?? null
  );

  constructor() {
    this.carregando.set(true);
    this.transacaoService.listarExtrato().subscribe({
      next: (lista) => {
        this.transacoes.set(lista.filter((transacao) => transacao.tipoTransacao === 'PIX'));
        this.carregando.set(false);
      },
      error: () => {
        this.erro.set('Nao foi possivel carregar o extrato.');
        this.carregando.set(false);
      }
    });
  }

  voltar(): void {
    this.location.back();
  }
}
