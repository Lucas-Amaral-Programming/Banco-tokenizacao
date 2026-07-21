import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  DestinatarioPixResponse,
  ResolverChavePixRequest,
  TransacaoRequest
} from '../models/transacao-request.model';
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

  resolverChavePix(request: ResolverChavePixRequest): Observable<DestinatarioPixResponse> {
    return this.http.post<DestinatarioPixResponse>('/api/chaves-pix/resolver', request);
  }
}
