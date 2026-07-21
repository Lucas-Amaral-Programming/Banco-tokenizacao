import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { LayoutBanco } from './layout-banco';

@Component({
  imports: [LayoutBanco],
  template: `
    <app-layout-banco>
      <header layout-cabecalho data-testid="cabecalho">Cabecalho</header>
      <article layout-conteudo data-testid="conteudo">Conteudo</article>
      <nav layout-navegacao data-testid="navegacao">Navegacao</nav>
    </app-layout-banco>
  `
})
class HospedeiroTeste {}

describe('LayoutBanco', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HospedeiroTeste]
    }).compileComponents();
  });

  it('renderiza o shell e os tres slots projetados', () => {
    const fixture = TestBed.createComponent(HospedeiroTeste);
    fixture.detectChanges();
    const elemento = fixture.nativeElement as HTMLElement;
    const folha = elemento.querySelector('.layout-banco__folha');

    expect(elemento.querySelector('main.layout-banco')).toBeTruthy();
    expect(elemento.querySelector('[data-testid="cabecalho"]')?.textContent)
      .toContain('Cabecalho');
    expect(folha?.querySelector('[data-testid="conteudo"]')?.textContent)
      .toContain('Conteudo');
    expect(elemento.querySelector('[data-testid="navegacao"]')?.textContent)
      .toContain('Navegacao');
  });
});
