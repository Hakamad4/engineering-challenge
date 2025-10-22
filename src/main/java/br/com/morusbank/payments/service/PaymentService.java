package br.com.morusbank.payments.service;

import br.com.morusbank.payments.domain.constant.MorusAccount;
import br.com.morusbank.payments.domain.entity.Payment;
import br.com.morusbank.payments.domain.entity.PropertyOwner;
import br.com.morusbank.payments.domain.entity.RealEstateAgency;
import br.com.morusbank.payments.domain.entity.Statement;
import br.com.morusbank.payments.domain.exception.*;
import br.com.morusbank.payments.domain.repository.PaymentRepository;
import br.com.morusbank.payments.domain.repository.PropertyOwnerRepository;
import br.com.morusbank.payments.domain.repository.RealEstateAgencyRepository;
import br.com.morusbank.payments.domain.repository.StatementRepository;
import br.com.morusbank.payments.domain.request.PaymentRequest;
import br.com.morusbank.payments.domain.response.PaymentResponse;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentRepository paymentRepository;
    private final PropertyOwnerRepository propertyOwnerRepository;
    private final RealEstateAgencyRepository realEstateAgencyRepository;
    private final MoneyTransferService moneyTransferService;
    private final StatementRepository statementRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          PropertyOwnerRepository propertyOwnerRepository,
                          RealEstateAgencyRepository realEstateAgencyRepository,
                          MoneyTransferService moneyTransferService,
                          StatementRepository statementRepository) {
        this.paymentRepository = paymentRepository;
        this.propertyOwnerRepository = propertyOwnerRepository;
        this.realEstateAgencyRepository = realEstateAgencyRepository;
        this.moneyTransferService = moneyTransferService;
        this.statementRepository = statementRepository;
    }

    @Transactional //o transaction garante indepotencia, mas no money transfer dependendo de como for o processo,
    // podemos garantir uma ambiguidade e rastreabilidade usando uma fila.
    public PaymentResponse processPayment(PaymentRequest request) throws PaymentException {
        log.info("Processando pagamento {}", request.externalReference());
        Payment payment = findOrCreatePayment(request);
        validatePayment(payment);

        PropertyOwner propertyOwner = findPropertyOwner(payment);
        RealEstateAgency realEstateAgency = findRealEstateAgency(payment);

        payment.setPropertyOwnerId(propertyOwner.getId());
        payment.setRealEstateAgencyId(realEstateAgency.getId());

        processStatements(payment, realEstateAgency, propertyOwner);

        payment.setStatus(Payment.Status.COMPLETED);
        paymentRepository.save(payment);

        log.info("Pagamento {} processado com sucesso", request.externalReference());
        return PaymentResponse.fromPayment(payment);
    }

    private void processStatements(Payment payment, RealEstateAgency realEstateAgency, PropertyOwner propertyOwner) throws PaymentException {
        List<Statement> statements = createStatements(payment, realEstateAgency, propertyOwner);

        validateStatementsIntegrity(payment.getAmount(), statements);

        for (Statement statement : statements) {
            Statement saved = statementRepository.save(statement);

            boolean isSuccess = transferMoney(statement);
            if (!isSuccess) {
                payment.setStatus(Payment.Status.FAILED);
                throw new PaymentException("Falha ao transferir valor para conta " + saved.getAccountId());
            }
        }
    }

    private void validateStatementsIntegrity(BigDecimal paymentAmount, List<Statement> statements) throws PaymentException {
        BigDecimal totalStatements = statements.stream()
                .map(Statement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (paymentAmount.compareTo(totalStatements) != 0) {
            log.error("Erro de integridade: Pagamento = {}, Soma dos statements = {}", paymentAmount, totalStatements);
            throw new PaymentException("Erro de integridade: a soma dos lançamentos não é igual ao valor do pagamento");
        }
    }

    private Payment findOrCreatePayment(PaymentRequest request) {
        Payment payment;
        Optional<Payment> optionalPayment = paymentRepository.findByExternalReference(request.externalReference());
        if (optionalPayment.isPresent()) {
            payment = optionalPayment.get();
        } else {
            payment = createPayment(request);
            payment = paymentRepository.save(payment);
        }
        return payment;
    }

    private Payment createPayment(PaymentRequest request) {
        Payment payment = new Payment();
        payment.setAmount(request.amount());
        payment.setPropertyOwnerId(request.propertyOwnerId());
        payment.setRealEstateAgencyId(request.realEstateAgencyId());
        payment.setExternalReference(request.externalReference());
        payment.setStatus(Payment.Status.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        return payment;
    }

    private RealEstateAgency findRealEstateAgency(Payment payment) throws RealEstateAgencyNotFoundException {
        return realEstateAgencyRepository.findById(payment.getRealEstateAgencyId())
                .orElseThrow(RealEstateAgencyNotFoundException::new);
    }

    public PropertyOwner findPropertyOwner(Payment payment) throws PropertyOwnerNotFoundException {
        return propertyOwnerRepository.findById(payment.getPropertyOwnerId())
                .orElseThrow(PropertyOwnerNotFoundException::new);
    }

    private List<Statement> createStatements(Payment payment, RealEstateAgency realEstateAgency, PropertyOwner propertyOwner) {
        BigDecimal morusAmount = payment.getAmount()
                .multiply(MorusAccount.MOUROSBANK_FEE_PERCENTAGE)
                .setScale(2, BigDecimal.ROUND_CEILING);

        BigDecimal agencyAmount = realEstateAgency.getFeePercentage()
                .multiply(payment.getAmount())
                .setScale(2, BigDecimal.ROUND_CEILING);

        BigDecimal ownerAmount = payment.getAmount()
                .subtract(morusAmount)
                .subtract(agencyAmount)
                .setScale(2, BigDecimal.ROUND_CEILING);

        Statement agencyStatement = createStatement(payment, "Recebimento de taxa de administração", agencyAmount, realEstateAgency.getAccountId());
        Statement ownerStatement = createStatement(payment, "Repasse de aluguel", ownerAmount, propertyOwner.getAccountId());
        Statement morusStatement = createStatement(payment, "Receita da plataforma", morusAmount, MorusAccount.getMorusAccount().getId());

        return List.of(agencyStatement, ownerStatement, morusStatement);
    }

    private Statement createStatement(Payment payment, String description, BigDecimal amount, Long accountId) {
        log.info("Criando statement para pagamento {}", payment.getExternalReference());
        Statement statement = new Statement();
        statement.setPaymentId(payment.getId());
        statement.setAccountId(accountId);
        statement.setAmount(amount);
        statement.setDescription(description);
        statement.setCreatedAt(payment.getCreatedAt());
        return statement;
    }

    private void validatePayment(Payment payment) throws PaymentException {
        isAlreadyProcessed(payment);
        isFailed(payment);
    }

    private void isAlreadyProcessed(Payment payment) throws PaymentAlreadyProcessedException {
        boolean isCompleted = payment.getStatus().equals(Payment.Status.COMPLETED);
        if (isCompleted) {
            throw new PaymentAlreadyProcessedException();
        }
    }

    private void isFailed(Payment payment) throws PaymentException {
        boolean isFailed = payment.getStatus().equals(Payment.Status.FAILED);
        if (isFailed) {
            log.warn("Pagamento {} já falhou anteriormente", payment.getExternalReference());
            throw new PaymentException("Pagamento já falhou anteriormente e não pode ser reprocessado");
        }
    }

    private boolean transferMoney(Statement statement) {
        boolean success = true;
        try {
            moneyTransferService.transferMoney(statement);
        } catch (Exception e) {
            log.error("Fail to transfer money to account {}", statement.getAccountId());
            success = false;
        }
        return success;
    }
}
