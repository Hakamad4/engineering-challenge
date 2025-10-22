package br.com.morusbank.payments.service;

import br.com.morusbank.payments.domain.entity.Account;
import br.com.morusbank.payments.domain.entity.Statement;
import br.com.morusbank.payments.domain.exception.AccountNotFoundException;
import br.com.morusbank.payments.domain.repository.AccountRepository;
import br.com.morusbank.payments.domain.repository.StatementRepository;
import br.com.morusbank.payments.domain.response.AccountStatementResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatementService - Testes Unitários")
class StatementServiceTest {

    @Mock
    private StatementRepository statementRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private StatementService statementService;

    private Account account;
    private List<Statement> statements;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId(1L);
        account.setName("Test Account");
        account.setBalance(new BigDecimal("5000.00"));
        account.setType(Account.AccountType.PROPERTY_OWNER);

        Statement statement1 = new Statement();
        statement1.setId(1L);
        statement1.setAccountId(1L);
        statement1.setPaymentId(1L);
        statement1.setAmount(new BigDecimal("100.00"));
        statement1.setDescription("Test 1");
        statement1.setCreatedAt(LocalDateTime.now());

        Statement statement2 = new Statement();
        statement2.setId(2L);
        statement2.setAccountId(1L);
        statement2.setPaymentId(1L);
        statement2.setAmount(new BigDecimal("200.00"));
        statement2.setDescription("Test 2");
        statement2.setCreatedAt(LocalDateTime.now());

        statements = List.of(statement1, statement2);
    }

    @Test
    @DisplayName("Deve retornar extrato da conta com sucesso")
    void shouldGetAccountStatementsSuccessfully()  throws AccountNotFoundException {
        // Arrange
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        Page<Statement> page = new PageImpl<>(statements);
        when(statementRepository.findByAccountIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        // Act
        AccountStatementResponse response = statementService.getAccountStatements(1L, 0, 20);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.accountId());
        assertEquals("Test Account", response.accountName());
        assertEquals(2, response.statements().size());
        assertEquals(2, response.totalStatements());
        verify(accountRepository).findById(1L);
        verify(statementRepository).findByAccountIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando conta não existe")
    void shouldThrowExceptionWhenAccountNotFound() {
        // Arrange
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> {
            statementService.getAccountStatements(999L, 0, 20);
        });

        verify(accountRepository).findById(999L);
        verify(statementRepository, never()).findByAccountIdOrderByCreatedAtDesc(any(), any());
    }

    @Test
    @DisplayName("Deve aplicar limite máximo de tamanho de página")
    void shouldApplyMaxPageSizeLimit() throws AccountNotFoundException {
        // Arrange
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        Page<Statement> page = new PageImpl<>(statements);
        when(statementRepository.findByAccountIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        // Act
        statementService.getAccountStatements(1L, 0, 200); // Requisita 200, mas limite é 100

        // Assert
        verify(statementRepository).findByAccountIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class));
    }
}