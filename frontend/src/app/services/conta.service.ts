import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import {
  CadastroContaRequest,
  ContaResponse,
  LoginContaRequest
} from '../models/conta.model';

const CHAVE_CONTA_ATUAL = 'contaAtual';

@Injectable({ providedIn: 'root' })
export class ContaService {
  private readonly http = inject(HttpClient);
  private readonly urlBase = 'http://localhost:8080/api/contas';

  private readonly contaAtualSignal = signal<ContaResponse | null>(this.lerContaSalva());

  readonly contaAtual = this.contaAtualSignal.asReadonly();
  readonly estaLogado = computed(() => this.contaAtualSignal() !== null);

  login(request: LoginContaRequest): Observable<ContaResponse> {
    return this.http.post<ContaResponse>(`${this.urlBase}/login`, request).pipe(
      tap((conta) => this.definirContaAtual(conta))
    );
  }

  cadastrar(request: CadastroContaRequest): Observable<ContaResponse> {
    return this.http.post<ContaResponse>(this.urlBase, request);
  }

  consultarConta(numeroConta: string): Observable<ContaResponse> {
    return this.http.get<ContaResponse>(`${this.urlBase}/${numeroConta}`).pipe(
      tap((conta) => this.definirContaAtual(conta))
    );
  }

  logout(): void {
    this.contaAtualSignal.set(null);
    localStorage.removeItem(CHAVE_CONTA_ATUAL);
  }

  private definirContaAtual(conta: ContaResponse): void {
    this.contaAtualSignal.set(conta);
    localStorage.setItem(CHAVE_CONTA_ATUAL, JSON.stringify(conta));
  }

  private lerContaSalva(): ContaResponse | null {
    const conteudo = localStorage.getItem(CHAVE_CONTA_ATUAL);
    return conteudo ? (JSON.parse(conteudo) as ContaResponse) : null;
  }
}
