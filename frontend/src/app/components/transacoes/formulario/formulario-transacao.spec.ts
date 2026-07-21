import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { Location } from '@angular/common';
import { provideRouter } from '@angular/router';
import { Observable, Subject, of } from 'rxjs';
import { FormularioTransacao } from './formulario-transacao';
import { TransacaoService } from '../../../services/transacao.service';
import { ContaService } from '../../../services/conta.service';
import { ContaResponse } from '../../../models/conta.model';
import {
  DestinatarioPixResponse,
  ResolverChavePixRequest,
  TransacaoRequest
} from '../../../models/transacao-request.model';
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
let ultimaResolucao: ResolverChavePixRequest | null;

function criarComponente(
  tipo: string,
  resolver: (request: ResolverChavePixRequest) => Observable<DestinatarioPixResponse> =
    () => of({ nomeTitular: 'Ana Souza', tipoChavePix: 'CELULAR' })
) {
  ultimaChamada = null;
  ultimaResolucao = null;
  TestBed.resetTestingModule();

  const transacaoStub: Partial<TransacaoService> = {
    criarTransacao: (request, chave) => {
      ultimaChamada = { request, chave };
      return of(RESPOSTA_OK);
    },
    resolverChavePix: (request) => {
      ultimaResolucao = request;
      return resolver(request);
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

  describe('deteccao automatica da chave PIX', () => {
    it('detecta e-mail, normaliza e resolve o destinatario', () => {
      const { component } = criarComponente('PIX');
      const exibido = digitar((e) => component.aoDigitarChave(e), 'ANA@EMAIL.COM');

      expect(component.estadoChave()).toBe('EMAIL');
      expect(component.numeroContaDestino).toBe('ana@email.com');
      expect(exibido).toBe('ANA@EMAIL.COM');
      expect(ultimaResolucao).toEqual({ tipoChavePix: 'EMAIL', chave: 'ana@email.com' });
      expect(component.nomeDestinatario).toBe('Ana Souza');
    });

    it('detecta e formata CPF valido sem regra de celular', () => {
      const { component } = criarComponente('PIX', (request) =>
        of({ nomeTitular: 'Ana Souza', tipoChavePix: request.tipoChavePix }));
      const exibido = digitar((e) => component.aoDigitarChave(e), '12345678909');

      expect(component.estadoChave()).toBe('CPF');
      expect(exibido).toBe('123.456.789-09');
      expect(ultimaResolucao?.tipoChavePix).toBe('CPF');
    });

    it('detecta e formata celular que nao e CPF valido', () => {
      const { component } = criarComponente('PIX', (request) =>
        of({ nomeTitular: 'Ana Souza', tipoChavePix: request.tipoChavePix }));
      const exibido = digitar((e) => component.aoDigitarChave(e), '11987654321');

      expect(component.estadoChave()).toBe('CELULAR');
      expect(exibido).toBe('(11) 98765-4321');
      expect(ultimaResolucao?.tipoChavePix).toBe('CELULAR');
    });

    it('pede escolha quando a chave pode ser CPF ou celular', () => {
      const { component } = criarComponente('PIX', (request) =>
        of({ nomeTitular: 'Ana Souza', tipoChavePix: request.tipoChavePix }));
      const exibido = digitar((e) => component.aoDigitarChave(e), '52998224725');

      expect(component.estadoChave()).toBe('AMBIGUA');
      expect(exibido).toBe('52998224725');
      expect(ultimaResolucao).toBeNull();

      component.selecionarTipoChave('CPF');
      expect(component.estadoChave()).toBe('CPF');
      expect(component.chaveExibida).toBe('529.982.247-25');
      expect(ultimaResolucao?.tipoChavePix).toBe('CPF');
    });

    it('marca como invalida quando nenhuma regra numerica se aplica', () => {
      const { component } = criarComponente('PIX');
      digitar((e) => component.aoDigitarChave(e), '12345678901');
      expect(component.estadoChave()).toBe('INVALIDA');
      expect(component.erroChave()).toBeTruthy();
      expect(ultimaResolucao).toBeNull();
    });

    it('trunca colagem numerica e volta a indefinida ao apagar', () => {
      const { component } = criarComponente('PIX', (request) =>
        of({ nomeTitular: 'Ana Souza', tipoChavePix: request.tipoChavePix }));
      digitar((e) => component.aoDigitarChave(e), '11987654321999');
      expect(component.numeroContaDestino).toBe('11987654321');

      digitar((e) => component.aoDigitarChave(e), '1199');
      expect(component.estadoChave()).toBe('INDEFINIDA');
      expect(component.nomeDestinatario).toBe('');
      expect(component.destinatarioResolvido()).toBe(false);
    });

    it('ignora resposta antiga quando a chave muda durante a consulta', () => {
      const primeira = new Subject<DestinatarioPixResponse>();
      const segunda = new Subject<DestinatarioPixResponse>();
      let chamadas = 0;
      const { component } = criarComponente('PIX', () => ++chamadas === 1 ? primeira : segunda);

      digitar((e) => component.aoDigitarChave(e), '11987654321');
      digitar((e) => component.aoDigitarChave(e), 'novo@email.com');
      segunda.next({ nomeTitular: 'Destinatario atual', tipoChavePix: 'EMAIL' });
      primeira.next({ nomeTitular: 'Resposta antiga', tipoChavePix: 'CELULAR' });

      expect(component.nomeDestinatario).toBe('Destinatario atual');
    });
  });

  describe('mascaras e valores', () => {
    it('formata o valor como moeda ao digitar', () => {
      const { component } = criarComponente('DEPOSITO');
      const exibido = digitar((e) => component.aoDigitarValor(e), '12345');
      expect(exibido).toBe('R$ 123,45');
    });

    it('nao preenche o nome com chave incompleta', () => {
      const { component } = criarComponente('PIX');
      digitar((e) => component.aoDigitarChave(e), '119999');

      expect(component.nomeDestinatario).toBe('');
    });
  });

  describe('envio da transacao', () => {
    it('PIX envia chave normalizada e tipo detectado', () => {
      const { component } = criarComponente('PIX', (request) =>
        of({ nomeTitular: 'Ana Souza', tipoChavePix: request.tipoChavePix }));
      digitar((e) => component.aoDigitarChave(e), '11987654321');
      component.valorTransacaoTexto = 'R$ 50,00';

      component.enviar();

      expect(ultimaChamada?.request.tipoTransacao).toBe('PIX');
      expect(ultimaChamada?.request.numeroContaDestino).toBe('11987654321');
      expect(ultimaChamada?.request.tipoChavePix).toBe('CELULAR');
      expect(ultimaChamada?.request.valorTransacao).toBe(50);
      expect(component.resposta()).toEqual(RESPOSTA_OK);
    });

    it('bloqueia PIX sem destinatario resolvido', () => {
      const { component } = criarComponente('PIX');
      component.valorTransacaoTexto = 'R$ 50,00';
      digitar((e) => component.aoDigitarChave(e), '12345678901');

      component.enviar();

      expect(ultimaChamada).toBeNull();
      expect(component.erro()).toBeTruthy();
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
      const { component } = criarComponente('PIX', (request) =>
        of({ nomeTitular: 'Ana Souza', tipoChavePix: request.tipoChavePix }));
      digitar((e) => component.aoDigitarChave(e), '11987654321');
      component.valorTransacaoTexto = 'R$ 50,00';
      component.descricao = 'almoco';

      component.enviar();

      expect(component.numeroContaDestino).toBe('');
      expect(component.chaveExibida).toBe('');
      expect(component.estadoChave()).toBe('INDEFINIDA');
      expect(component.valorTransacaoTexto).toBe('R$ 0,00');
      expect(component.descricao).toBe('');
      expect(component.nomeDestinatario).toBe('');
    });
  });
});
