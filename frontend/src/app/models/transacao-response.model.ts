import { TipoTransacao } from './transacao-request.model';

export type StatusTransacao = 'PENDENTE' | 'APROVADA' | 'RECUSADA';

export interface TransacaoResponse {
  tokenTransacao: string;
  tipoTransacao: TipoTransacao;
  numeroContaOrigem: string | null;
  numeroContaDestino: string | null;
  valorTransacao: number;
  descricaoTransacao: string | null;
  statusTransacao: StatusTransacao;
  dataHoraTransacao: string;
}
