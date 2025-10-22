package br.com.morusbank.payments.domain.exception;

public class PaymentAlreadyProcessedException extends PaymentException {

    public PaymentAlreadyProcessedException() {
        super("Payment already processed");
    }

    public PaymentAlreadyProcessedException(String message) {
        super(message);
    }

    public PaymentAlreadyProcessedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaymentAlreadyProcessedException(Throwable cause) {
        super(cause);
    }

    public PaymentAlreadyProcessedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
