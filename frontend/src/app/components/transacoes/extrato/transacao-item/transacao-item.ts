import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { TransacaoResponse } from '../../../../models/transacao-response.model';
import { Icone } from '../../../shared/icone/icone';

@Component({
  selector: 'app-transacao-item',
  imports: [CurrencyPipe, DatePipe, Icone],
  templateUrl: './transacao-item.html',
  styleUrl: './transacao-item.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransacaoItem {
  readonly transacao = input.required<TransacaoResponse>();
  readonly numeroContaAtual = input<string | null>(null);

  protected readonly contraParte = computed(() => {
    const transacao = this.transacao();
    const enviada = transacao.numeroContaOrigem === this.numeroContaAtual();
    const outra = enviada ? transacao.numeroContaDestino : transacao.numeroContaOrigem;
    return outra ?? '-';
  });

  protected readonly classeStatus = computed(() => {
    switch (this.transacao().statusTransacao) {
      case 'APROVADA':
        return 'ok';
      case 'RECUSADA':
        return 'erro';
      default:
        return 'pendente';
    }
  });
}
