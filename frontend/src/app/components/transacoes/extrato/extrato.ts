import { Component, computed, inject, signal } from '@angular/core';
import { Location } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TransacaoResponse } from '../../../models/transacao-response.model';
import { TransacaoService } from '../../../services/transacao.service';
import { ContaService } from '../../../services/conta.service';
import { Icone } from '../../shared/icone/icone';
import { LayoutBanco } from '../../shared/layout-banco/layout-banco';
import { BottomNav } from '../../home/bottom-nav/bottom-nav';
import { ExtratoResumo } from './resumo/extrato-resumo';
import { TransacaoItem } from './transacao-item/transacao-item';

@Component({
  selector: 'app-extrato',
  imports: [RouterLink, Icone, BottomNav, ExtratoResumo, TransacaoItem, LayoutBanco],
  templateUrl: './extrato.html',
  styleUrl: './extrato.scss'
})
export class Extrato {
  private readonly transacaoService = inject(TransacaoService);
  private readonly contaService = inject(ContaService);
  private readonly location = inject(Location);

  protected readonly transacoes = signal<TransacaoResponse[]>([]);
  protected readonly carregando = signal(false);
  protected readonly erro = signal<string | null>(null);

  protected readonly numeroContaAtual = computed(
    () => this.contaService.contaAtual()?.numeroConta ?? null
  );

  private readonly datasOrdenadas = computed(() =>
    this.transacoes()
      .map((transacao) => transacao.dataHoraTransacao)
      .sort()
  );

  protected readonly periodoInicio = computed(() => this.datasOrdenadas().at(0) ?? null);
  protected readonly periodoFim = computed(() => this.datasOrdenadas().at(-1) ?? null);

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

  voltar(): void {
    this.location.back();
  }
}
