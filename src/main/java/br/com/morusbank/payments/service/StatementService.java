package br.com.morusbank.payments.service;

import br.com.morusbank.payments.domain.entity.Account;
import br.com.morusbank.payments.domain.entity.Statement;
import br.com.morusbank.payments.domain.exception.AccountNotFoundException;
import br.com.morusbank.payments.domain.repository.AccountRepository;
import br.com.morusbank.payments.domain.repository.StatementRepository;
import br.com.morusbank.payments.domain.response.AccountStatementResponse;
import br.com.morusbank.payments.domain.response.StatementResponse;
import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StatementService {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(StatementService.class);
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final StatementRepository statementRepository;
    private final AccountRepository accountRepository;

    public StatementService(StatementRepository statementRepository,
                            AccountRepository accountRepository) {
        this.statementRepository = statementRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public AccountStatementResponse getAccountStatements(Long accountId, Integer page, Integer size) throws AccountNotFoundException {
        log.info("Buscando extrato da conta: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        int pageNumber = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? Math.min(size, MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<Statement> statementPage = statementRepository.findByAccountIdOrderByCreatedAtDesc(accountId, pageable);

        List<StatementResponse> statementResponses = statementPage.getContent().stream()
                .map(StatementResponse::fromStatement)
                .toList();

        log.info("Encontrados {} statements para conta {}", statementPage.getTotalElements(), accountId);

        return new AccountStatementResponse(
                account.getId(),
                account.getName(),
                account.getBalance(),
                statementPage.getTotalElements(),
                statementPage.getNumber(),
                statementPage.getSize(),
                statementPage.getTotalPages(),
                statementResponses
        );
    }


    @Transactional(readOnly = true)
    public AccountStatementResponse getAccountStatementsByDateRange(
            Long accountId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Integer page,
            Integer size) throws AccountNotFoundException {

        log.info("Buscando extrato da conta {} entre {} e {}", accountId, startDate, endDate);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        int pageNumber = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? Math.min(size, MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<Statement> statementPage = statementRepository.findByAccountIdAndDateRange(
                accountId, startDate, endDate, pageable
        );

        List<StatementResponse> statementResponses = statementPage.getContent().stream()
                .map(StatementResponse::fromStatement)
                .toList();

        log.info("Encontrados {} statements para conta {} no período", statementPage.getTotalElements(), accountId);

        return new AccountStatementResponse(
                account.getId(),
                account.getName(),
                account.getBalance(),
                statementPage.getTotalElements(),
                statementPage.getNumber(),
                statementPage.getSize(),
                statementPage.getTotalPages(),
                statementResponses
        );
    }
}
