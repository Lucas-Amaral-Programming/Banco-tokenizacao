import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, catchError, map, of, switchMap, tap } from 'rxjs';
import {
  CadastroContaRequest,
  ContaResponse,
  LoginContaRequest
} from '../models/conta.model';

@Injectable({ providedIn: 'root' })
export class ContaService {
  private readonly http = inject(HttpClient);
  private readonly urlContas = '/api/contas';
  private readonly urlAuth = '/api/auth';

  private readonly contaAtualSignal = signal<ContaResponse | null>(null);

  readonly contaAtual = this.contaAtualSignal.asReadonly();
  readonly estaLogado = computed(() => this.contaAtualSignal() !== null);

  iniciarCsrf(): Observable<void> {
    return this.http.get<void>(`${this.urlAuth}/csrf`);
  }

  carregarSessao(): Observable<ContaResponse | null> {
    return this.http.get<ContaResponse>(`${this.urlAuth}/me`).pipe(
      tap((conta) => this.contaAtualSignal.set(conta)),
      catchError(() => {
        this.contaAtualSignal.set(null);
        return of(null);
      })
    );
  }

  login(request: LoginContaRequest): Observable<ContaResponse> {
    return this.http.post<ContaResponse>(`${this.urlAuth}/login`, request).pipe(
      tap((conta) => this.contaAtualSignal.set(conta)),
      switchMap((conta) => this.iniciarCsrf().pipe(map(() => conta)))
    );
  }

  cadastrar(request: CadastroContaRequest): Observable<ContaResponse> {
    return this.http.post<ContaResponse>(this.urlContas, request);
  }

  atualizarSaldo(): Observable<ContaResponse> {
    return this.http.get<ContaResponse>(`${this.urlContas}/me`).pipe(
      tap((conta) => this.contaAtualSignal.set(conta))
    );
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.urlAuth}/logout`, {}).pipe(
      tap(() => this.limparSessao()),
      switchMap(() => this.iniciarCsrf())
    );
  }

  limparSessao(): void {
    this.contaAtualSignal.set(null);
  }
}
