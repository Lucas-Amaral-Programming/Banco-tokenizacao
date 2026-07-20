import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { TipoTransacao } from '../../models/transacao-request.model';
import { ContaService } from '../../services/conta.service';

@Component({
  selector: 'app-transacoes',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './transacoes.html',
  styleUrl: './transacoes.scss'
})
export class Transacoes {
  private readonly contaService = inject(ContaService);
  private readonly router = inject(Router);

  protected readonly contaAtual = this.contaService.contaAtual;
  protected readonly saldoVisivel = signal(false);
  protected readonly carregandoSaldo = signal(false);

  protected readonly tiposTransacao: TipoTransacao[] = [
    'PIX',
    'DEPOSITO',
    'SAQUE'
  ];

  verSaldo(): void {
    this.carregandoSaldo.set(true);
    this.contaService.atualizarSaldo().subscribe({
      next: () => {
        this.saldoVisivel.set(true);
        this.carregandoSaldo.set(false);
      },
      error: () => this.carregandoSaldo.set(false)
    });
  }

  sair(): void {
    this.contaService.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login'])
    });
  }
}
