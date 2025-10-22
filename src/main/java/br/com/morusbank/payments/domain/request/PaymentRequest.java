package br.com.morusbank.payments.domain.request;

import java.math.BigDecimal;

public record PaymentRequest(
        Long propertyOwnerId,
        Long realEstateAgencyId,
        String externalReference,
        BigDecimal amount,
        String description) {

}
