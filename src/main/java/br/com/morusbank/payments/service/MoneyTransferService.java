package br.com.morusbank.payments.service;

import br.com.morusbank.payments.domain.entity.Account;
import br.com.morusbank.payments.domain.entity.Statement;
import br.com.morusbank.payments.domain.exception.PaymentException;
import br.com.morusbank.payments.domain.repository.AccountRepository;
import br.com.morusbank.payments.domain.request.MoneyTransferMessageRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class MoneyTransferService {

    private static final Logger log = LoggerFactory.getLogger(MoneyTransferService.class);
    private final AccountRepository accountRepository;

    public MoneyTransferService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void transferMoney(Statement statement) throws PaymentException {
        log.info("Iniciando transferência de R$ {} para conta {}", statement.getAmount(), statement.getAccountId());

        Account account = accountRepository.findById(statement.getAccountId())
                .orElseThrow(() -> new PaymentException("Conta não encontrada"));

        BigDecimal currentBalance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
        BigDecimal newBalance = currentBalance.add(statement.getAmount());

        account.setBalance(newBalance);
        accountRepository.save(account);

        log.info("Transferência concluída. Novo saldo da conta {}: R$ {}", account.getId(), newBalance);
    }

}
