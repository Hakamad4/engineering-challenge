package br.com.morusbank.payments.service;

import br.com.morusbank.payments.domain.entity.Account;
import br.com.morusbank.payments.domain.entity.Statement;
import br.com.morusbank.payments.domain.exception.PaymentException;
import br.com.morusbank.payments.domain.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MoneyTransferService - Testes Unitários")
class MoneyTransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private MoneyTransferService moneyTransferService;

    private Account account;
    private Statement statement;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId(1L);
        account.setName("Conta Teste");
        account.setBalance(new BigDecimal("1000.00"));
        account.setType(Account.AccountType.PROPERTY_OWNER);

        statement = new Statement();
        statement.setId(1L);
        statement.setPaymentId(1L);
        statement.setAccountId(1L);
        statement.setAmount(new BigDecimal("500.00"));
        statement.setDescription("Teste");
        statement.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve transferir dinheiro e atualizar saldo da conta")
    void shouldTransferMoneyAndUpdateBalance() throws PaymentException {
        // Arrange
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);

        // Act
        moneyTransferService.transferMoney(statement);

        // Assert
        verify(accountRepository).save(accountCaptor.capture());
        Account updatedAccount = accountCaptor.getValue();

        assertEquals(new BigDecimal("1500.00"), updatedAccount.getBalance());
    }

    @Test
    @DisplayName("Deve lançar exceção quando conta não existe")
    void shouldThrowExceptionWhenAccountNotFound() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PaymentException.class, () -> {
            moneyTransferService.transferMoney(statement);
        });

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Deve lidar com saldo nulo e inicializar com zero")
    void shouldHandleNullBalanceAndInitializeWithZero() throws PaymentException {
        account.setBalance(null);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);

        moneyTransferService.transferMoney(statement);

        verify(accountRepository).save(accountCaptor.capture());
        Account updatedAccount = accountCaptor.getValue();

        assertEquals(new BigDecimal("500.00"), updatedAccount.getBalance());
    }
}
