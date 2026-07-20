import { Component, inject } from '@angular/core';
import { ContaCard } from './conta-card';
import { ContaService } from '../../../services/conta.service';

@Component({
  selector: 'app-home-contas',
  imports: [ContaCard],
  templateUrl: './home-contas.html',
  styleUrl: './home-contas.scss'
})
export class HomeContas {
  private readonly contaService = inject(ContaService);
  protected readonly conta = this.contaService.contaAtual;
}
