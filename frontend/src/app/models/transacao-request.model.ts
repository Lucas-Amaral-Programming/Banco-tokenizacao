export type TipoTransacao = 'PIX' | 'DEPOSITO' | 'SAQUE';

export type TipoChavePix = 'CPF' | 'EMAIL' | 'CELULAR' | 'COPIA_COLA';

export interface TransacaoRequest {
  tipoTransacao: TipoTransacao;
  numeroContaDestino: string;
  tipoChavePix: TipoChavePix | null;
  valorTransacao: number;
  descricaoTransacao: string;
}
