import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { ContaResponse, TipoConta } from '../../../models/conta.model';
import { Icone, NomeIcone } from '../../shared/icone/icone';

@Component({
  selector: 'app-conta-origem',
  imports: [CurrencyPipe, Icone],
  templateUrl: './conta-origem.html',
  styleUrl: './conta-origem.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ContaOrigem {
  readonly conta = input.required<ContaResponse | null>();
  readonly rotulo = input('Conta de origem');
  readonly icone = input<NomeIcone>('cardholder');

  protected readonly rotulosTipo: Record<TipoConta, string> = {
    CORRENTE: 'Conta Corrente',
    POUPANCA: 'Poupança'
  };
}
