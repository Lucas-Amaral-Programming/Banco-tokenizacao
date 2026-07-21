import { Component, input, model, signal } from '@angular/core';

export type IconeCampo = 'usuario' | 'documento' | 'banco' | 'cadeado' | 'email' | 'celular';
export type TipoCampo = 'texto' | 'senha' | 'select';
export type MascaraCampo = 'cpf' | 'celular' | 'moeda' | 'chave-pix' | null;

@Component({
  selector: 'app-campo-formulario',
  imports: [],
  templateUrl: './campo-formulario.html',
  styleUrl: './campo-formulario.scss'
})
export class CampoFormulario {
  readonly rotulo = input.required<string>();
  readonly icone = input.required<IconeCampo>();
  readonly tipo = input<TipoCampo>('texto');
  readonly placeholder = input('');
  readonly opcoes = input<string[]>([]);
  readonly ajuda = input('');
  readonly mascara = input<MascaraCampo>(null);

  readonly valor = model<string>('');

  protected readonly mostrarSenha = signal(false);

  protected get tipoInput(): string {
    return this.tipo() === 'senha' && !this.mostrarSenha() ? 'password' : 'text';
  }

  alternarSenha(): void {
    this.mostrarSenha.update((visivel) => !visivel);
  }

  atualizar(evento: Event): void {
    const alvo = evento.target as HTMLInputElement | HTMLSelectElement;
    let valorAtual = alvo.value;

    if (this.mascara() === 'cpf') {
      valorAtual = this.formatarCpf(valorAtual);
      alvo.value = valorAtual;
    } else if (this.mascara() === 'celular') {
      valorAtual = this.formatarTelefone(valorAtual);
      alvo.value = valorAtual;
    } else if (this.mascara() === 'moeda') {
      valorAtual = this.formatarMoeda(valorAtual);
      alvo.value = valorAtual;
    } else if (this.mascara() === 'chave-pix' && !this.pareceEmail(valorAtual)) {
      valorAtual = this.formatarCpf(valorAtual);
      alvo.value = valorAtual;
    }

    this.valor.set(valorAtual);
  }

  private pareceEmail(valor: string): boolean {
    return /[a-zA-Z@]/.test(valor);
  }

  private formatarCpf(valor: string): string {
    const digitos = valor.replace(/\D/g, '').slice(0, 11);
    if (digitos.length <= 3) {
      return digitos;
    }
    if (digitos.length <= 6) {
      return `${digitos.slice(0, 3)}.${digitos.slice(3)}`;
    }
    if (digitos.length <= 9) {
      return `${digitos.slice(0, 3)}.${digitos.slice(3, 6)}.${digitos.slice(6)}`;
    }
    return `${digitos.slice(0, 3)}.${digitos.slice(3, 6)}.${digitos.slice(6, 9)}-${digitos.slice(9)}`;
  }

  private formatarTelefone(valor: string): string {
    const digitos = valor.replace(/\D/g, '').slice(0, 11);
    if (digitos.length === 0) {
      return '';
    }
    if (digitos.length <= 2) {
      return `(${digitos}`;
    }
    if (digitos.length <= 7) {
      return `(${digitos.slice(0, 2)}) ${digitos.slice(2)}`;
    }
    return `(${digitos.slice(0, 2)}) ${digitos.slice(2, 7)}-${digitos.slice(7)}`;
  }

  private formatarMoeda(valor: string): string {
    const digitos = valor.replace(/\D/g, '').slice(0, 13);
    const centavosTotais = digitos === '' ? 0 : parseInt(digitos, 10);
    const reais = Math.floor(centavosTotais / 100);
    const centavos = String(centavosTotais % 100).padStart(2, '0');
    return `R$ ${reais.toLocaleString('pt-BR')},${centavos}`;
  }
}

