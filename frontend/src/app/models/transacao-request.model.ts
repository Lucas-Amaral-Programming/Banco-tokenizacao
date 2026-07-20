export type TipoTransacao = 'PIX' | 'DEPOSITO' | 'SAQUE';

export interface TransacaoRequest {
  tipoTransacao: TipoTransacao;
  numeroContaOrigem: string;
  numeroContaDestino: string;
  valorTransacao: number;
  descricaoTransacao: string;
}
