package br.com.morusbank.payments.domain.exception;

public class PaymentFailedException extends PaymentException {

    public PaymentFailedException() {
        super("O pagamento houve uma falha e não é valido.");
    }

    public PaymentFailedException(String message) {
        super(message);
    }

    public PaymentFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaymentFailedException(Throwable cause) {
        super(cause);
    }

    public PaymentFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
