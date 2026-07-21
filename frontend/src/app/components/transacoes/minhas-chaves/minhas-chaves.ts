import { Component, inject, signal } from '@angular/core';
import { Location } from '@angular/common';
import { ChavePixResponse } from '../../../models/chave-pix.model';
import { TipoChavePix } from '../../../models/transacao-request.model';
import { TransacaoService } from '../../../services/transacao.service';
import { Icone, NomeIcone } from '../../shared/icone/icone';
import { LayoutBanco } from '../../shared/layout-banco/layout-banco';
import { BottomNav } from '../../home/bottom-nav/bottom-nav';

@Component({
  selector: 'app-minhas-chaves',
  imports: [Icone, LayoutBanco, BottomNav],
  templateUrl: './minhas-chaves.html',
  styleUrl: './minhas-chaves.scss'
})
export class MinhasChaves {
  private readonly transacaoService = inject(TransacaoService);
  private readonly location = inject(Location);

  protected readonly chaves = signal<ChavePixResponse[]>([]);
  protected readonly carregando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly aviso = signal<string | null>(null);

  private readonly rotulos: Record<TipoChavePix, string> = {
    CPF: 'CPF',
    EMAIL: 'E-mail',
    CELULAR: 'Celular'
  };

  private readonly icones: Record<TipoChavePix, NomeIcone> = {
    CPF: 'conta',
    EMAIL: 'envelope',
    CELULAR: 'telefone'
  };

  constructor() {
    this.carregando.set(true);
    this.transacaoService.listarMinhasChaves().subscribe({
      next: (lista) => {
        this.chaves.set(lista);
        this.carregando.set(false);
      },
      error: () => {
        this.erro.set('Nao foi possivel carregar suas chaves.');
        this.carregando.set(false);
      }
    });
  }

  voltar(): void {
    this.location.back();
  }

  rotulo(tipo: TipoChavePix): string {
    return this.rotulos[tipo];
  }

  icone(tipo: TipoChavePix): NomeIcone {
    return this.icones[tipo];
  }

  valorFormatado(chave: ChavePixResponse): string {
    if (chave.tipoChavePix === 'CPF') {
      return chave.chave.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
    }
    if (chave.tipoChavePix === 'CELULAR') {
      return chave.chave.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
    }
    return chave.chave;
  }

  private avisoTimeout: ReturnType<typeof setTimeout> | null = null;

  copiar(valor: string): void {
    navigator.clipboard?.writeText(valor);
    this.aviso.set('Chave copiada.');
    if (this.avisoTimeout) {
      clearTimeout(this.avisoTimeout);
    }
    this.avisoTimeout = setTimeout(() => this.aviso.set(null), 2000);
  }
}
