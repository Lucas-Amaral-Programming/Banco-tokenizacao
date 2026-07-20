export type TipoTransacao = 'PIX' | 'DEPOSITO' | 'SAQUE';

export interface TransacaoRequest {
  tipoTransacao: TipoTransacao;
  numeroContaDestino: string;
  valorTransacao: number;
  descricaoTransacao: string;
}
