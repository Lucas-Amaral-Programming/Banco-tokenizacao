import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { TransacaoRequest } from '../models/transacao-request.model';
import { TransacaoResponse } from '../models/transacao-response.model';

@Injectable({ providedIn: 'root' })
export class TransacaoService {
  private readonly http = inject(HttpClient);
  private readonly urlBase = 'http://localhost:8080/api/transacoes';

  criarTransacao(request: TransacaoRequest): Observable<TransacaoResponse> {
    return this.http.post<TransacaoResponse>(this.urlBase, request);
  }

  listarPorConta(numeroConta: string): Observable<TransacaoResponse[]> {
    return this.http.get<TransacaoResponse[]>(`${this.urlBase}?conta=${numeroConta}`);
  }
}
