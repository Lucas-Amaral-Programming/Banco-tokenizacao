import { Component, input } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { ContaResponse, TipoConta } from '../../../models/conta.model';

@Component({
  selector: 'app-conta-card',
  imports: [CurrencyPipe],
  templateUrl: './conta-card.html',
  styleUrl: './conta-card.scss'
})
export class ContaCard {
  readonly conta = input.required<ContaResponse>();
  readonly saldoVisivel = input(true);

  protected readonly rotulosTipo: Record<TipoConta, string> = {
    CORRENTE: 'Conta Corrente',
    POUPANCA: 'Poupança'
  };
}
