package br.com.morusbank.payments.domain.exception;

public class PropertyOwnerNotFoundException extends PaymentException {

    public PropertyOwnerNotFoundException() {
        super("Proprietario não encontrado");
    }

    public PropertyOwnerNotFoundException(String message) {
        super(message);
    }

    public PropertyOwnerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PropertyOwnerNotFoundException(Throwable cause) {
        super(cause);
    }

    public PropertyOwnerNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
