package br.com.foursys.tokenizacao.transacoes.exception;

public class ConflitoConcorrenciaException extends RuntimeException {

    public ConflitoConcorrenciaException(Throwable causa) {
        super("Conflito de concorrencia ao processar a transacao. Tente novamente.", causa);
    }
}
