package br.com.morusbank.payments.domain.exception;

public class RealEstateAgencyNotFoundException extends PaymentException {

    public RealEstateAgencyNotFoundException() {
        super("Imobiliária não encontrada");
    }

    public RealEstateAgencyNotFoundException(String message) {
        super(message);
    }

    public RealEstateAgencyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public RealEstateAgencyNotFoundException(Throwable cause) {
        super(cause);
    }

    public RealEstateAgencyNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
