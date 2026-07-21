import { By } from '@angular/platform-browser';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Observable, Subject, of, throwError } from 'rxjs';
import { CadastroContaRequest, ContaResponse } from '../../models/conta.model';
import { ContaService } from '../../services/conta.service';
import { LayoutAuth } from '../shared/layout-auth/layout-auth';
import { Cadastro } from './cadastro';

const DADOS_VALIDOS: CadastroContaRequest = {
  nomeTitular: 'Maria Silva',
  cpf: '529.982.247-25',
  telefone: '(11) 99999-9999',
  email: 'maria@email.com',
  tipoConta: 'POUPANCA',
  senha: 'senha123'
};

const CONTA_CRIADA: ContaResponse = {
  numeroConta: '12345',
  nomeTitular: 'Maria Silva',
  tipoConta: 'POUPANCA',
  saldoConta: 0,
  statusConta: 'ATIVA'
};

describe('Cadastro', () => {
  let chamadas: CadastroContaRequest[];
  let resposta$: Observable<ContaResponse>;

  beforeEach(async () => {
    chamadas = [];
    resposta$ = of(CONTA_CRIADA);

    await TestBed.configureTestingModule({
      imports: [Cadastro],
      providers: [
        provideRouter([]),
        {
          provide: ContaService,
          useValue: {
            cadastrar: (request: CadastroContaRequest) => {
              chamadas.push({ ...request });
              return resposta$;
            }
          }
        }
      ]
    }).compileComponents();
  });

  afterEach(() => TestBed.resetTestingModule());

  function criarComponente() {
    const fixture = TestBed.createComponent(Cadastro);
    fixture.detectChanges();
    return { fixture, component: fixture.componentInstance as any };
  }

  function preencherDadosValidos(component: any) {
    component.dados = { ...DADOS_VALIDOS };
  }

  it('renderiza os campos e aplica a variante de destaque', () => {
    const { fixture } = criarComponente();
    const layout = fixture.debugElement.query(By.directive(LayoutAuth));

    expect(fixture.nativeElement.querySelectorAll('app-campo-formulario')).toHaveLength(6);
    expect(layout.componentInstance.variante()).toBe('cadastro-destaque');
    expect(fixture.nativeElement.querySelector('a[href="/login"]')).toBeTruthy();
  });

  it('bloqueia CPF invalido antes de chamar o servico', () => {
    const { component } = criarComponente();
    preencherDadosValidos(component);
    component.dados.cpf = '111.111.111-11';

    component.cadastrar();

    expect(component.erros().cpf).toBe('CPF invalido.');
    expect(chamadas).toHaveLength(0);
  });

  it('bloqueia celular invalido antes de chamar o servico', () => {
    const { component } = criarComponente();
    preencherDadosValidos(component);
    component.dados.telefone = '(11) 88888-8888';

    component.cadastrar();

    expect(component.erros().telefone).toBe('Celular invalido.');
    expect(chamadas).toHaveLength(0);
  });

  it('bloqueia e-mail invalido antes de chamar o servico', () => {
    const { component } = criarComponente();
    preencherDadosValidos(component);
    component.dados.email = 'maria@invalido';

    component.cadastrar();

    expect(component.erros().email).toBe('E-mail invalido.');
    expect(chamadas).toHaveLength(0);
  });

  it('bloqueia nome incompleto antes de chamar o servico', () => {
    const { component } = criarComponente();
    preencherDadosValidos(component);
    component.dados.nomeTitular = 'Maria';

    component.cadastrar();

    expect(component.erros().nomeTitular).toBe('Informe o nome completo (nome e sobrenome).');
    expect(chamadas).toHaveLength(0);
  });

  it('exige os campos obrigatorios preenchidos', () => {
    const { component } = criarComponente();
    component.dados = { nomeTitular: '', cpf: '', telefone: '', email: '', tipoConta: 'CORRENTE', senha: '' };

    component.cadastrar();

    expect(component.erros()).toEqual({
      nomeTitular: 'Informe o nome do titular.',
      cpf: 'Informe o CPF.',
      telefone: 'Informe o celular.',
      email: 'Informe o e-mail.',
      senha: 'Informe a senha.'
    });
    expect(chamadas).toHaveLength(0);
  });

  it('envia os dados e exibe o estado de carregamento', () => {
    const pendente = new Subject<ContaResponse>();
    resposta$ = pendente;
    const { fixture, component } = criarComponente();
    preencherDadosValidos(component);

    component.cadastrar();
    fixture.detectChanges();

    const botao = fixture.nativeElement.querySelector(
      'button[type="submit"]'
    ) as HTMLButtonElement;
    expect(chamadas).toEqual([DADOS_VALIDOS]);
    expect(component.carregando()).toBe(true);
    expect(botao.disabled).toBe(true);
    expect(botao.textContent).toContain('Cadastrando...');

    pendente.next(CONTA_CRIADA);
    pendente.complete();
  });

  it('destaca o campo informado pela API e reabilita o formulario', () => {
    resposta$ = throwError(() => ({
      error: { mensagem: 'Ja existe uma conta para o CPF informado.', campo: 'cpf' }
    }));
    const { fixture, component } = criarComponente();
    preencherDadosValidos(component);

    component.cadastrar();
    fixture.detectChanges();

    expect(component.erros().cpf).toBe('Ja existe uma conta para o CPF informado.');
    expect(component.carregando()).toBe(false);
    expect(fixture.nativeElement.querySelector('[role="alert"]')?.textContent)
      .toContain('Ja existe uma conta para o CPF informado.');
  });

  it('usa o erro geral quando a API nao informa o campo', () => {
    resposta$ = throwError(() => ({ error: { mensagem: 'Falha inesperada.' } }));
    const { fixture, component } = criarComponente();
    preencherDadosValidos(component);

    component.cadastrar();
    fixture.detectChanges();

    expect(component.erroGeral()).toBe('Falha inesperada.');
    expect(component.carregando()).toBe(false);
    expect(fixture.nativeElement.querySelector('.erro[role="alert"]')?.textContent)
      .toContain('Falha inesperada.');
  });

  it('usa a mensagem padrao quando a API nao informa o motivo', () => {
    resposta$ = throwError(() => ({}));
    const { component } = criarComponente();
    preencherDadosValidos(component);

    component.cadastrar();

    expect(component.erroGeral()).toBe('Nao foi possivel cadastrar a conta.');
    expect(component.carregando()).toBe(false);
  });

  it('substitui o formulario pela confirmacao apos sucesso', () => {
    const { fixture, component } = criarComponente();
    preencherDadosValidos(component);

    component.cadastrar();
    fixture.detectChanges();

    const sucesso = fixture.nativeElement.querySelector('.sucesso');
    expect(component.contaCriada()).toEqual(CONTA_CRIADA);
    expect(component.carregando()).toBe(false);
    expect(sucesso?.textContent).toContain('Cadastro concluido!');
    expect(sucesso?.querySelector('a[href="/login"]')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('form')).toBeNull();
  });
});
