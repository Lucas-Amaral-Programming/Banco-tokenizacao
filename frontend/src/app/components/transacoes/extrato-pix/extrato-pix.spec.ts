import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { Location } from '@angular/common';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ExtratoPix } from './extrato-pix';
import { TransacaoService } from '../../../services/transacao.service';
import { ContaService } from '../../../services/conta.service';
import { ContaResponse } from '../../../models/conta.model';
import { TransacaoResponse } from '../../../models/transacao-response.model';

const CONTA_LOGADA: ContaResponse = {
  numeroConta: '00011',
  nomeTitular: 'Maria',
  tipoConta: 'CORRENTE',
  saldoConta: 1000,
  statusConta: 'ATIVA'
};

function tx(tipo: TransacaoResponse['tipoTransacao'], token: string): TransacaoResponse {
  return {
    tokenTransacao: token,
    tipoTransacao: tipo,
    numeroContaOrigem: '00011',
    numeroContaDestino: '00022',
    valorTransacao: 10,
    descricaoTransacao: null,
    statusTransacao: 'APROVADA',
    dataHoraTransacao: '2026-07-20T10:00:00'
  };
}

function criarComponente(extrato = of<TransacaoResponse[]>([])) {
  TestBed.resetTestingModule();
  TestBed.configureTestingModule({
    imports: [ExtratoPix],
    providers: [
      provideRouter([]),
      { provide: TransacaoService, useValue: { listarExtrato: () => extrato } },
      { provide: ContaService, useValue: { contaAtual: signal<ContaResponse | null>(CONTA_LOGADA) } },
      { provide: Location, useValue: { back: () => {} } }
    ]
  });
  const fixture = TestBed.createComponent(ExtratoPix);
  fixture.detectChanges();
  return { fixture, component: fixture.componentInstance as any };
}

describe('ExtratoPix', () => {
  afterEach(() => TestBed.resetTestingModule());

  it('mostra apenas transacoes PIX', () => {
    const { component } = criarComponente(
      of([tx('PIX', 'a'), tx('DEPOSITO', 'b'), tx('PIX', 'c'), tx('SAQUE', 'd')])
    );

    expect(component.transacoes().map((t: TransacaoResponse) => t.tokenTransacao)).toEqual(['a', 'c']);
    expect(component.carregando()).toBe(false);
  });

  it('exibe erro quando a consulta falha', () => {
    const { component } = criarComponente(throwError(() => new Error('falha')));

    expect(component.erro()).toBe('Nao foi possivel carregar o extrato.');
    expect(component.carregando()).toBe(false);
  });
});
