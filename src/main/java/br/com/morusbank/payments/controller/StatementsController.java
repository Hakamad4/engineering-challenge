package br.com.morusbank.payments.controller;

import br.com.morusbank.payments.domain.exception.AccountNotFoundException;
import br.com.morusbank.payments.domain.exception.PaymentException;
import br.com.morusbank.payments.domain.response.AccountStatementResponse;
import br.com.morusbank.payments.service.StatementService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/statements")
public class StatementsController {

    public final StatementService statementService;

    public StatementsController(StatementService statementService) {
        this.statementService = statementService;
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountStatementResponse> getAccountStatements(
            @PathVariable("accountId") Integer accountId,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) throws AccountNotFoundException {

        return ResponseEntity.ok(statementService.getAccountStatements(Long.valueOf(accountId), page, size));
    }

    @GetMapping("/{accountId}/filter")
    public ResponseEntity<?> getAccountStatementsByDateRange(
            @PathVariable("accountId") Integer accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) throws AccountNotFoundException {

        return ResponseEntity.ok(statementService.getAccountStatementsByDateRange(
                Long.valueOf(accountId), startDate, endDate, page, size
        ));
    }
}
