package br.com.morusbank.payments.domain.exception;

import br.com.morusbank.payments.domain.response.ErrorResponse;

public class PaymentException extends Exception {

    private ErrorResponse errorResponse;

    public PaymentException(String message) {
        super(message);
        errorResponse = new ErrorResponse(message);
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
        errorResponse = new ErrorResponse(message);
    }

    public PaymentException(Throwable cause) {
        super(cause);
        errorResponse = new ErrorResponse("Internal Error: " + cause.getMessage());
    }

    public PaymentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        errorResponse = new ErrorResponse(message);
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }
}
