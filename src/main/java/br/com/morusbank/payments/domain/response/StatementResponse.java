package br.com.morusbank.payments.domain.response;

import br.com.morusbank.payments.domain.entity.Statement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StatementResponse(
        Long id,
        Long paymentId,
        Long accountId,
        BigDecimal amount,
        String description,
        LocalDateTime createdAt
) {
    public static StatementResponse fromStatement(Statement statement) {
        return new StatementResponse(
                statement.getId(),
                statement.getPaymentId(),
                statement.getAccountId(),
                statement.getAmount(),
                statement.getDescription(),
                statement.getCreatedAt()
        );
    }
}