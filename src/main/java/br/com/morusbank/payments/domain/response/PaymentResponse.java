package br.com.morusbank.payments.domain.response;

import br.com.morusbank.payments.domain.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        String externalReference,
        BigDecimal amount,
        Long realEstateAgencyId,
        Long propertyOwnerId,
        String description,
        LocalDateTime createdAt
) {
    public static PaymentResponse fromPayment(Payment payment) {
        return new PaymentResponse(
                payment.getExternalReference(),
                payment.getAmount(),
                payment.getRealEstateAgencyId(),
                payment.getPropertyOwnerId(),
                null,
                payment.getCreatedAt()
        );
    }
}