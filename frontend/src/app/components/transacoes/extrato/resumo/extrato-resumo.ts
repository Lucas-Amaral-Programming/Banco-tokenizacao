import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Icone } from '../../../shared/icone/icone';

@Component({
  selector: 'app-extrato-resumo',
  imports: [DatePipe, Icone],
  templateUrl: './extrato-resumo.html',
  styleUrl: './extrato-resumo.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExtratoResumo {
  readonly inicio = input<string | null>(null);
  readonly fim = input<string | null>(null);
  readonly total = input<number>(0);
}
