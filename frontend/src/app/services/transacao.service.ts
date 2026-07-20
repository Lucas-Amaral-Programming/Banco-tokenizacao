import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { TransacaoRequest } from '../models/transacao-request.model';
import { TransacaoResponse } from '../models/transacao-response.model';

@Injectable({ providedIn: 'root' })
export class TransacaoService {
  private readonly http = inject(HttpClient);
  private readonly urlBase = '/api/transacoes';

  criarTransacao(request: TransacaoRequest, idempotencyKey: string): Observable<TransacaoResponse> {
    return this.http.post<TransacaoResponse>(this.urlBase, request, {
      headers: { 'Idempotency-Key': idempotencyKey }
    });
  }

  listarExtrato(): Observable<TransacaoResponse[]> {
    return this.http.get<TransacaoResponse[]>(this.urlBase);
  }
}
