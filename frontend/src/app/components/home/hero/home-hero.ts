import { Component, computed, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ContaService } from '../../../services/conta.service';

@Component({
  selector: 'app-home-hero',
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './home-hero.html',
  styleUrl: './home-hero.scss'
})
export class HomeHero {
  private readonly contaService = inject(ContaService);

  protected readonly conta = this.contaService.contaAtual;
  protected readonly saldo = computed(() => this.conta()?.saldoConta ?? 0);
  protected readonly saldoVisivel = signal(true);

  alternarSaldo(): void {
    this.saldoVisivel.update((visivel) => !visivel);
  }
}
