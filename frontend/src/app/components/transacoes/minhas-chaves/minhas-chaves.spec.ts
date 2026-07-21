import { TestBed } from '@angular/core/testing';
import { Location } from '@angular/common';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { MinhasChaves } from './minhas-chaves';
import { TransacaoService } from '../../../services/transacao.service';
import { ChavePixResponse } from '../../../models/chave-pix.model';

const CHAVES: ChavePixResponse[] = [
  { tipoChavePix: 'CPF', chave: '52998224725' },
  { tipoChavePix: 'EMAIL', chave: 'maria@email.com' },
  { tipoChavePix: 'CELULAR', chave: '11987654321' }
];

function criarComponente(resposta = of<ChavePixResponse[]>(CHAVES)) {
  TestBed.resetTestingModule();
  TestBed.configureTestingModule({
    imports: [MinhasChaves],
    providers: [
      provideRouter([]),
      { provide: TransacaoService, useValue: { listarMinhasChaves: () => resposta } },
      { provide: Location, useValue: { back: () => {} } }
    ]
  });
  const fixture = TestBed.createComponent(MinhasChaves);
  fixture.detectChanges();
  return { fixture, component: fixture.componentInstance as any };
}

describe('MinhasChaves', () => {
  afterEach(() => TestBed.resetTestingModule());

  it('carrega as chaves da conta', () => {
    const { component } = criarComponente();

    expect(component.chaves()).toHaveLength(3);
    expect(component.carregando()).toBe(false);
  });

  it('formata CPF e celular e mantem o e-mail', () => {
    const { component } = criarComponente();

    expect(component.valorFormatado({ tipoChavePix: 'CPF', chave: '52998224725' })).toBe('529.982.247-25');
    expect(component.valorFormatado({ tipoChavePix: 'CELULAR', chave: '11987654321' })).toBe('(11) 98765-4321');
    expect(component.valorFormatado({ tipoChavePix: 'EMAIL', chave: 'maria@email.com' })).toBe('maria@email.com');
  });

  it('sinaliza ao copiar uma chave', () => {
    const { component } = criarComponente();

    component.copiar('52998224725');
    expect(component.aviso()).toBe('Chave copiada.');
  });

  it('exibe erro quando a consulta falha', () => {
    const { component } = criarComponente(throwError(() => new Error('falha')));

    expect(component.erro()).toBe('Nao foi possivel carregar suas chaves.');
    expect(component.carregando()).toBe(false);
  });
});
