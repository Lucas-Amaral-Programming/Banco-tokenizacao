import { Component } from '@angular/core';
import { CurrencyPipe } from '@angular/common';

@Component({
  selector: 'app-home-cartoes',
  imports: [CurrencyPipe],
  templateUrl: './home-cartoes.html',
  styleUrl: './home-cartoes.scss'
})
export class HomeCartoes {
  // Mock visual: nao ha entidade de cartao no backend ainda.
  protected readonly cartao = {
    nome: 'Cartão Foursys',
    final: '1234',
    faturaAtual: 1280.9
  };
}
