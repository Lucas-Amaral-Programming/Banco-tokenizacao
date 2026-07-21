import { Component, inject } from '@angular/core';
import { Location } from '@angular/common';
import { Icone, NomeIcone } from '../../shared/icone/icone';
import { LayoutBanco } from '../../shared/layout-banco/layout-banco';
import { BottomNav } from '../../home/bottom-nav/bottom-nav';

interface Limite {
  icone: NomeIcone;
  titulo: string;
  descricao: string;
  valor: string;
}

@Component({
  selector: 'app-meus-limites',
  imports: [Icone, LayoutBanco, BottomNav],
  templateUrl: './meus-limites.html',
  styleUrl: './meus-limites.scss'
})
export class MeusLimites {
  private readonly location = inject(Location);

  protected readonly limites: Limite[] = [
    {
      icone: 'pix',
      titulo: 'Por transação',
      descricao: 'Valor máximo em um único Pix',
      valor: 'R$ 5.000,00'
    },
    {
      icone: 'olho',
      titulo: 'Diário (6h às 20h)',
      descricao: 'Total que você pode enviar durante o dia',
      valor: 'R$ 10.000,00'
    },
    {
      icone: 'olho-fechado',
      titulo: 'Noturno (20h às 6h)',
      descricao: 'Total que você pode enviar durante a noite',
      valor: 'R$ 1.000,00'
    }
  ];

  voltar(): void {
    this.location.back();
  }
}
