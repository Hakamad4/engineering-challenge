package br.com.morusbank.payments.service;

import br.com.morusbank.payments.domain.entity.Account;
import br.com.morusbank.payments.domain.entity.Payment;
import br.com.morusbank.payments.domain.entity.PropertyOwner;
import br.com.morusbank.payments.domain.entity.RealEstateAgency;
import br.com.morusbank.payments.domain.entity.Statement;
import br.com.morusbank.payments.domain.exception.PaymentAlreadyProcessedException;
import br.com.morusbank.payments.domain.exception.PaymentException;
import br.com.morusbank.payments.domain.exception.PropertyOwnerNotFoundException;
import br.com.morusbank.payments.domain.exception.RealEstateAgencyNotFoundException;
import br.com.morusbank.payments.domain.repository.PaymentRepository;
import br.com.morusbank.payments.domain.repository.PropertyOwnerRepository;
import br.com.morusbank.payments.domain.repository.RealEstateAgencyRepository;
import br.com.morusbank.payments.domain.repository.StatementRepository;
import br.com.morusbank.payments.domain.request.PaymentRequest;
import br.com.morusbank.payments.domain.response.PaymentResponse;
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
@DisplayName("PaymentService - Testes Unitários")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PropertyOwnerRepository propertyOwnerRepository;

    @Mock
    private RealEstateAgencyRepository realEstateAgencyRepository;

    @Mock
    private StatementRepository statementRepository;

    @Mock
    private MoneyTransferService moneyTransferService;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest validRequest;
    private Payment pendingPayment;
    private PropertyOwner propertyOwner;
    private RealEstateAgency realEstateAgency;
    private Account ownerAccount;
    private Account agencyAccount;
    private Account morusAccount;

    @BeforeEach
    void setUp() {
        // Request válido
        validRequest = new PaymentRequest(
                1L,
                1L,
                "PAY-2345",
                new BigDecimal("3000.00"),
                "Pagamento de aluguel"
        );

        // Payment pendente
        pendingPayment = new Payment();
        pendingPayment.setId(1L);
        pendingPayment.setExternalReference("PAY-2345");
        pendingPayment.setAmount(new BigDecimal("3000.00"));
        pendingPayment.setPropertyOwnerId(1L);
        pendingPayment.setRealEstateAgencyId(1L);
        pendingPayment.setStatus(Payment.Status.PENDING);
        pendingPayment.setCreatedAt(LocalDateTime.now());

        // Contas
        ownerAccount = new Account();
        ownerAccount.setId(1L);
        ownerAccount.setName("Proprietário");
        ownerAccount.setBalance(BigDecimal.ZERO);
        ownerAccount.setType(Account.AccountType.PROPERTY_OWNER);

        agencyAccount = new Account();
        agencyAccount.setId(2L);
        agencyAccount.setName("Imobiliária");
        agencyAccount.setBalance(BigDecimal.ZERO);
        agencyAccount.setType(Account.AccountType.REAL_ESTATE_AGENCY);

        morusAccount = new Account();
        morusAccount.setId(3L);
        morusAccount.setName("Morus");
        morusAccount.setBalance(BigDecimal.ZERO);
        morusAccount.setType(Account.AccountType.PLATFORM_REVENUE);

        // PropertyOwner
        propertyOwner = new PropertyOwner();
        propertyOwner.setId(1L);
        propertyOwner.setName("João da Silva");
        propertyOwner.setAccountId(1L);
        propertyOwner.setRealEstateAgencyId(1L);

        // RealEstateAgency
        realEstateAgency = new RealEstateAgency();
        realEstateAgency.setId(1L);
        realEstateAgency.setName("Imobiliária XYZ");
        realEstateAgency.setFeePercentage(new BigDecimal("0.10")); // 10%
        realEstateAgency.setAccountId(2L);
    }

    @Test
    @DisplayName("Deve processar pagamento com sucesso")
    void shouldProcessPaymentSuccessfully() throws PaymentException {
        // Arrange
        when(paymentRepository.findByExternalReference("PAY-2345")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(pendingPayment);
        when(propertyOwnerRepository.findById(1L)).thenReturn(Optional.of(propertyOwner));
        when(realEstateAgencyRepository.findById(1L)).thenReturn(Optional.of(realEstateAgency));
        when(statementRepository.save(any(Statement.class))).thenAnswer(i -> i.getArguments()[0]);
        doNothing().when(moneyTransferService).transferMoney(any(Statement.class));

        // Act
        PaymentResponse response = paymentService.processPayment(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals("PAY-2345", response.externalReference());
        assertEquals(new BigDecimal("3000.00"), response.amount());
        verify(paymentRepository, times(2)).save(any(Payment.class));
        verify(statementRepository, times(3)).save(any(Statement.class));
        verify(moneyTransferService, times(3)).transferMoney(any(Statement.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar processar pagamento já completado (idempotência)")
    void shouldThrowExceptionWhenPaymentAlreadyCompleted() throws PaymentException {
        // Arrange
        Payment completedPayment = new Payment();
        completedPayment.setId(1L);
        completedPayment.setExternalReference("PAY-2345");
        completedPayment.setStatus(Payment.Status.COMPLETED);
        completedPayment.setCreatedAt(LocalDateTime.now());

        when(paymentRepository.findByExternalReference("PAY-2345"))
                .thenReturn(Optional.of(completedPayment));

        // Act & Assert
        assertThrows(PaymentAlreadyProcessedException.class, () -> {
            paymentService.processPayment(validRequest);
        });

        verify(moneyTransferService, never()).transferMoney(any(Statement.class));
        verify(statementRepository, never()).save(any(Statement.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando pagamento já falhou anteriormente")
    void shouldThrowExceptionWhenPaymentAlreadyFailed() {
        // Arrange
        Payment failedPayment = new Payment();
        failedPayment.setId(1L);
        failedPayment.setExternalReference("PAY-2345");
        failedPayment.setStatus(Payment.Status.FAILED);
        failedPayment.setCreatedAt(LocalDateTime.now());

        when(paymentRepository.findByExternalReference("PAY-2345"))
                .thenReturn(Optional.of(failedPayment));

        // Act & Assert
        assertThrows(PaymentException.class, () -> {
            paymentService.processPayment(validRequest);
        });
    }

    @Test
    @DisplayName("Deve lançar exceção quando proprietário não existe")
    void shouldThrowExceptionWhenPropertyOwnerNotFound() {
        // Arrange
        when(paymentRepository.findByExternalReference("PAY-2345")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(pendingPayment);
        when(propertyOwnerRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PropertyOwnerNotFoundException.class, () -> {
            paymentService.processPayment(validRequest);
        });
    }

    @Test
    @DisplayName("Deve lançar exceção quando imobiliária não existe")
    void shouldThrowExceptionWhenRealEstateAgencyNotFound() {
        // Arrange
        when(paymentRepository.findByExternalReference("PAY-2345")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(pendingPayment);
        when(propertyOwnerRepository.findById(1L)).thenReturn(Optional.of(propertyOwner));
        when(realEstateAgencyRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RealEstateAgencyNotFoundException.class, () -> {
            paymentService.processPayment(validRequest);
        });
    }

    @Test
    @DisplayName("Deve lançar exceção quando transferência falha")
    void shouldThrowExceptionWhenTransferFails() throws PaymentException {
        // Arrange
        when(paymentRepository.findByExternalReference("PAY-2345")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(pendingPayment);
        when(propertyOwnerRepository.findById(1L)).thenReturn(Optional.of(propertyOwner));
        when(realEstateAgencyRepository.findById(1L)).thenReturn(Optional.of(realEstateAgency));
        when(statementRepository.save(any(Statement.class))).thenAnswer(i -> i.getArguments()[0]);
        doThrow(new PaymentException("Conta não encontrada"))
                .when(moneyTransferService).transferMoney(any(Statement.class));

        // Act & Assert
        assertThrows(PaymentException.class, () -> {
            paymentService.processPayment(validRequest);
        });
    }

    @Test
    @DisplayName("Deve criar statements com valores corretos")
    void shouldCreateStatementsWithCorrectAmounts() throws PaymentException {
        // Arrange
        when(paymentRepository.findByExternalReference("PAY-2345")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(pendingPayment);
        when(propertyOwnerRepository.findById(1L)).thenReturn(Optional.of(propertyOwner));
        when(realEstateAgencyRepository.findById(1L)).thenReturn(Optional.of(realEstateAgency));

        ArgumentCaptor<Statement> statementCaptor = ArgumentCaptor.forClass(Statement.class);
        when(statementRepository.save(statementCaptor.capture())).thenAnswer(i -> i.getArguments()[0]);
        doNothing().when(moneyTransferService).transferMoney(any(Statement.class));

        // Act
        paymentService.processPayment(validRequest);

        // Assert
        var statements = statementCaptor.getAllValues();
        assertEquals(3, statements.size());

        // Verifica se a soma dos statements é igual ao valor do pagamento
        BigDecimal totalStatements = statements.stream()
                .map(Statement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(new BigDecimal("3000.00"), totalStatements);

        // Verifica os valores individuais (considerando arredondamento)
        assertTrue(statements.stream().anyMatch(s ->
                s.getAmount().compareTo(new BigDecimal("300.00")) == 0)); // 10% agência
        assertTrue(statements.stream().anyMatch(s ->
                s.getAmount().compareTo(new BigDecimal("60.00")) == 0)); // 2% morus
        assertTrue(statements.stream().anyMatch(s ->
                s.getAmount().compareTo(new BigDecimal("2640.00")) == 0)); // 88% proprietário
    }
}