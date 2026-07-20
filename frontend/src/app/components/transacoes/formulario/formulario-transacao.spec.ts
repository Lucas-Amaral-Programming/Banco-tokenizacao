import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { Location } from '@angular/common';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { FormularioTransacao } from './formulario-transacao';
import { TransacaoService } from '../../../services/transacao.service';
import { ContaService } from '../../../services/conta.service';
import { ContaResponse } from '../../../models/conta.model';
import { TransacaoRequest } from '../../../models/transacao-request.model';
import { TransacaoResponse } from '../../../models/transacao-response.model';

const CONTA_LOGADA: ContaResponse = {
  numeroConta: '12345',
  nomeTitular: 'Maria',
  tipoConta: 'CORRENTE',
  saldoConta: 1000,
  statusConta: 'ATIVA'
};

const RESPOSTA_OK: TransacaoResponse = {
  tokenTransacao: 'tok-1',
  tipoTransacao: 'PIX',
  numeroContaOrigem: '12345',
  numeroContaDestino: '999',
  valorTransacao: 10,
  descricaoTransacao: null,
  statusTransacao: 'APROVADA',
  dataHoraTransacao: '2026-07-20T10:00:00'
};

// Captura o par (request, chave) enviado ao servico para inspecao nos testes.
let ultimaChamada: { request: TransacaoRequest; chave: string } | null;

function criarComponente(tipo: string) {
  ultimaChamada = null;
  TestBed.resetTestingModule();

  const transacaoStub: Partial<TransacaoService> = {
    criarTransacao: (request, chave) => {
      ultimaChamada = { request, chave };
      return of(RESPOSTA_OK);
    }
  };

  const contaStub = { contaAtual: signal<ContaResponse | null>(CONTA_LOGADA) };

  TestBed.configureTestingModule({
    imports: [FormularioTransacao],
    providers: [
      provideRouter([]),
      { provide: TransacaoService, useValue: transacaoStub },
      { provide: ContaService, useValue: contaStub },
      { provide: Location, useValue: { back: () => {} } }
    ]
  });

  const fixture = TestBed.createComponent(FormularioTransacao);
  fixture.componentRef.setInput('tipo', tipo);
  fixture.detectChanges();
  return { fixture, component: fixture.componentInstance as any };
}

// Simula a digitacao num campo, disparando o handler com um input real.
function digitar(handler: (evento: Event) => void, valor: string) {
  const input = document.createElement('input');
  input.value = valor;
  handler({ target: input } as unknown as Event);
  return input.value;
}

describe('FormularioTransacao', () => {
  afterEach(() => {
    sessionStorage.clear();
    TestBed.resetTestingModule();
  });

  describe('resolucao do tipo pela rota', () => {
    it('reconhece PIX, DEPOSITO e SAQUE (inclusive minusculo)', () => {
      expect(criarComponente('pix').component.tipoTransacao()).toBe('PIX');
      expect(criarComponente('DEPOSITO').component.tipoTransacao()).toBe('DEPOSITO');
      expect(criarComponente('saque').component.tipoTransacao()).toBe('SAQUE');
    });

    it('retorna null para tipo invalido', () => {
      expect(criarComponente('boleto').component.tipoTransacao()).toBeNull();
    });
  });

  describe('origem e destino por tipo', () => {
    it('PIX exige origem e destino', () => {
      const { component } = criarComponente('PIX');
      expect(component.exigeOrigem()).toBe(true);
      expect(component.exigeDestino()).toBe(true);
    });

    it('DEPOSITO exige apenas destino', () => {
      const { component } = criarComponente('DEPOSITO');
      expect(component.exigeOrigem()).toBe(false);
      expect(component.exigeDestino()).toBe(true);
    });

    it('SAQUE exige apenas origem', () => {
      const { component } = criarComponente('SAQUE');
      expect(component.exigeOrigem()).toBe(true);
      expect(component.exigeDestino()).toBe(false);
    });
  });

  describe('tela PIX - tipo de chave', () => {
    it('ajusta icone e placeholder conforme o tipo de chave', () => {
      const { component } = criarComponente('PIX');

      component.selecionarTipoChave('CPF');
      expect(component.iconeChave()).toBe('conta');
      expect(component.placeholderChave()).toBe('000.000.000-00');

      component.selecionarTipoChave('EMAIL');
      expect(component.iconeChave()).toBe('envelope');

      component.selecionarTipoChave('CELULAR');
      expect(component.iconeChave()).toBe('telefone');
    });

    it('limpa a chave e o nome ao trocar de tipo', () => {
      const { component } = criarComponente('PIX');
      component.numeroContaDestino = '11122233344';
      component.nomeDestinatario = 'Ana Souza';

      component.selecionarTipoChave('EMAIL');

      expect(component.numeroContaDestino).toBe('');
      expect(component.nomeDestinatario).toBe('');
    });
  });

  describe('mascaras e auto-preenchimento', () => {
    it('formata o valor como moeda ao digitar', () => {
      const { component } = criarComponente('DEPOSITO');
      const exibido = digitar((e) => component.aoDigitarValor(e), '12345');
      expect(exibido).toBe('R$ 123,45');
    });

    it('formata CPF e preenche o nome quando a chave fica completa', () => {
      const { component } = criarComponente('PIX');
      component.selecionarTipoChave('CPF');

      const exibido = digitar((e) => component.aoDigitarChave(e), '11122233344');

      expect(exibido).toBe('111.222.333-44');
      expect(component.nomeDestinatario).toBe('Ana Souza');
    });

    it('nao preenche o nome com chave incompleta', () => {
      const { component } = criarComponente('PIX');
      component.selecionarTipoChave('CELULAR');

      digitar((e) => component.aoDigitarChave(e), '119999');

      expect(component.nomeDestinatario).toBe('');
    });
  });

  describe('envio da transacao', () => {
    it('PIX envia a chave digitada como conta de destino', () => {
      const { component } = criarComponente('PIX');
      component.numeroContaDestino = '67890';
      component.valorTransacaoTexto = 'R$ 50,00';

      component.enviar();

      expect(ultimaChamada?.request.tipoTransacao).toBe('PIX');
      expect(ultimaChamada?.request.numeroContaDestino).toBe('67890');
      expect(ultimaChamada?.request.valorTransacao).toBe(50);
      expect(component.resposta()).toEqual(RESPOSTA_OK);
    });

    it('DEPOSITO credita a propria conta logada', () => {
      const { component } = criarComponente('DEPOSITO');
      component.valorTransacaoTexto = 'R$ 200,00';

      component.enviar();

      expect(ultimaChamada?.request.numeroContaDestino).toBe(CONTA_LOGADA.numeroConta);
      expect(ultimaChamada?.request.valorTransacao).toBe(200);
    });

    it('SAQUE nao informa conta de destino', () => {
      const { component } = criarComponente('SAQUE');
      component.valorTransacaoTexto = 'R$ 30,00';

      component.enviar();

      expect(ultimaChamada?.request.numeroContaDestino).toBe('');
      expect(ultimaChamada?.request.valorTransacao).toBe(30);
    });

    it('limpa os campos apos o envio bem-sucedido', () => {
      const { component } = criarComponente('PIX');
      component.numeroContaDestino = '67890';
      component.valorTransacaoTexto = 'R$ 50,00';
      component.descricao = 'almoco';
      component.nomeDestinatario = 'Ana Souza';

      component.enviar();

      expect(component.numeroContaDestino).toBe('');
      expect(component.valorTransacaoTexto).toBe('R$ 0,00');
      expect(component.descricao).toBe('');
      expect(component.nomeDestinatario).toBe('');
    });
  });
});
