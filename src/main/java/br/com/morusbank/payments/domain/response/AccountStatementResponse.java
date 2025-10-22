package br.com.morusbank.payments.domain.response;

import java.math.BigDecimal;
import java.util.List;

public record AccountStatementResponse(
        Long accountId,
        String accountName,
        BigDecimal currentBalance,
        Long totalStatements,
        Integer pageNumber,
        Integer pageSize,
        Integer totalPages,
        List<StatementResponse> statements
) {
}