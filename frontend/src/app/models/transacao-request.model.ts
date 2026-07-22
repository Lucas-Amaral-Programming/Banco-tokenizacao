export type TipoTransacao = 'PIX' | 'DEPOSITO' | 'SAQUE';

export type TipoChavePix = 'CPF' | 'EMAIL' | 'CELULAR';

export interface ResolverChavePixRequest {
  tipoChavePix: TipoChavePix;
  chave: string;
}

export interface DestinatarioPixResponse {
  nomeTitular: string;
  cpfMascarado: string;
  tipoChavePix: TipoChavePix;
}

export interface TransacaoRequest {
  tipoTransacao: TipoTransacao;
  numeroContaDestino: string;
  tipoChavePix: TipoChavePix | null;
  valorTransacao: number;
  descricaoTransacao: string;
}
