import { TestBed } from '@angular/core/testing';
import { Location } from '@angular/common';
import { provideRouter } from '@angular/router';
import { MeusLimites } from './meus-limites';

describe('MeusLimites', () => {
  afterEach(() => TestBed.resetTestingModule());

  it('renderiza os limites informativos', () => {
    TestBed.configureTestingModule({
      imports: [MeusLimites],
      providers: [provideRouter([]), { provide: Location, useValue: { back: () => {} } }]
    });
    const fixture = TestBed.createComponent(MeusLimites);
    fixture.detectChanges();

    const itens = fixture.nativeElement.querySelectorAll('.limite');
    expect(itens.length).toBe(3);
    expect(fixture.nativeElement.querySelector('.hero__titulo').textContent).toContain('Meus limites');
  });
});
