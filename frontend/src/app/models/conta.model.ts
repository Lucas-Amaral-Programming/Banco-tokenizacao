export type TipoConta = 'CORRENTE' | 'POUPANCA';

export type StatusConta = 'ATIVA' | 'BLOQUEADA' | 'ENCERRADA';

export interface LoginContaRequest {
  cpf: string;
  senha: string;
}

export interface CadastroContaRequest {
  nomeTitular: string;
  cpf: string;
  email: string;
  tipoConta: TipoConta;
  senha: string;
}

export interface ContaResponse {
  numeroConta: string;
  nomeTitular: string;
  tipoConta: TipoConta;
  saldoConta: number;
  statusConta: StatusConta;
}
