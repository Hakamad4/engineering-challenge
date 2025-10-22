package br.com.morusbank.payments.integration;

import br.com.morusbank.payments.domain.entity.Account;
import br.com.morusbank.payments.domain.entity.Payment;
import br.com.morusbank.payments.domain.entity.PropertyOwner;
import br.com.morusbank.payments.domain.entity.RealEstateAgency;
import br.com.morusbank.payments.domain.entity.Statement;
import br.com.morusbank.payments.domain.repository.AccountRepository;
import br.com.morusbank.payments.domain.repository.PaymentRepository;
import br.com.morusbank.payments.domain.repository.PropertyOwnerRepository;
import br.com.morusbank.payments.domain.repository.RealEstateAgencyRepository;
import br.com.morusbank.payments.domain.repository.StatementRepository;
import br.com.morusbank.payments.domain.response.AccountStatementResponse;
import br.com.morusbank.payments.domain.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static br.com.morusbank.payments.domain.entity.Account.AccountType.*;
import static br.com.morusbank.payments.domain.entity.Payment.Status.COMPLETED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class StatementsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private StatementRepository statementRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RealEstateAgencyRepository realEstateAgencyRepository;

    @Autowired
    private PropertyOwnerRepository propertyOwnerRepository;

    private Account propertyOwnerAccount;
    private Account realEstateAgencyAccount;
    private Account platformAccount;
    private Payment payment;

    @BeforeEach
    void setUp() {
        // Limpar dados
        statementRepository.deleteAll();
        paymentRepository.deleteAll();
        propertyOwnerRepository.deleteAll();
        realEstateAgencyRepository.deleteAll();
        accountRepository.deleteAll();

        // Criar contas
        propertyOwnerAccount = new Account();
        propertyOwnerAccount.setName("João da Silva");
        propertyOwnerAccount.setType(PROPERTY_OWNER);
        propertyOwnerAccount.setBalance(BigDecimal.valueOf(2640.00));
        propertyOwnerAccount = accountRepository.save(propertyOwnerAccount);

        realEstateAgencyAccount = new Account();
        realEstateAgencyAccount.setName("Imobiliária XYZ");
        realEstateAgencyAccount.setType(REAL_ESTATE_AGENCY);
        realEstateAgencyAccount.setBalance(BigDecimal.valueOf(300.00));
        realEstateAgencyAccount = accountRepository.save(realEstateAgencyAccount);

        platformAccount = new Account();
        platformAccount.setName("Morus Platform");
        platformAccount.setType(PLATFORM_REVENUE);
        platformAccount.setBalance(BigDecimal.valueOf(60.00));
        platformAccount = accountRepository.save(platformAccount);

        // Criar imobiliária
        RealEstateAgency realEstateAgency = new RealEstateAgency();
        realEstateAgency.setName("Imobiliária XYZ");
        realEstateAgency.setFeePercentage(BigDecimal.valueOf(0.10));
        realEstateAgency.setAccountId(realEstateAgencyAccount.getId());
        realEstateAgency = realEstateAgencyRepository.save(realEstateAgency);

        // Criar proprietário
        PropertyOwner propertyOwner = new PropertyOwner();
        propertyOwner.setName("João da Silva");
        propertyOwner.setAccountId(propertyOwnerAccount.getId());
        propertyOwner.setRealEstateAgencyId(realEstateAgency.getId());
        propertyOwner = propertyOwnerRepository.save(propertyOwner);

        // Criar pagamento
        payment = new Payment();
        payment.setExternalReference("PAY-202501");
        payment.setAmount(BigDecimal.valueOf(3000.00));
        payment.setPropertyOwnerId(propertyOwner.getId());
        payment.setRealEstateAgencyId(realEstateAgency.getId());
        payment.setStatus(COMPLETED);
        payment.setCreatedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        // Criar statements
        Statement statement1 = new Statement();
        statement1.setPaymentId(payment.getId());
        statement1.setAccountId(propertyOwnerAccount.getId());
        statement1.setAmount(BigDecimal.valueOf(2640.00));
        statement1.setDescription("Repasse de aluguel");
        statement1.setCreatedAt(LocalDateTime.now().minusMonths(1L));
        statementRepository.save(statement1);

        Statement statement2 = new Statement();
        statement2.setPaymentId(payment.getId());
        statement2.setAccountId(realEstateAgencyAccount.getId());
        statement2.setAmount(BigDecimal.valueOf(300.00));
        statement2.setDescription("Taxa de administração");
        statement2.setCreatedAt(LocalDateTime.now().minusMonths(1L));
        statementRepository.save(statement2);

        Statement statement3 = new Statement();
        statement3.setPaymentId(payment.getId());
        statement3.setAccountId(platformAccount.getId());
        statement3.setAmount(BigDecimal.valueOf(60.00));
        statement3.setDescription("Receita da plataforma");
        statement3.setCreatedAt(LocalDateTime.now().minusMonths(1L));
        statementRepository.save(statement3);
    }

    @Test
    @DisplayName("Deve retornar extrato da conta com sucesso")
    void shouldGetAccountStatements() throws Exception {
        Long accountId = propertyOwnerAccount.getId();

        MvcResult result = mockMvc.perform(get("/api/v1/statements/{accountId}", accountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.accountName").value("João da Silva"))
                .andExpect(jsonPath("$.currentBalance").value(2640.00))
                .andExpect(jsonPath("$.totalStatements").value(1))
                .andExpect(jsonPath("$.statements[0].amount").value(2640.00))
                .andExpect(jsonPath("$.statements[0].description").value("Repasse de aluguel"))
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        AccountStatementResponse response = objectMapper.readValue(jsonResponse, AccountStatementResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.accountId()).isEqualTo(accountId);
        assertThat(response.statements()).hasSize(1);
    }

    @Test
    @DisplayName("Deve retornar extrato com paginação customizada")
    void shouldGetAccountStatementsWithCustomPagination() throws Exception {
        Long accountId = realEstateAgencyAccount.getId();
        
        // Criar mais statements para testar paginação
        for (int i = 0; i < 5; i++) {
            Statement statement = new Statement();
            statement.setPaymentId(payment.getId());
            statement.setAccountId(realEstateAgencyAccount.getId());
            statement.setAmount(BigDecimal.valueOf(100.00 + i));
            statement.setDescription("Statement " + i);
            statement.setCreatedAt(LocalDateTime.now().minusHours(i));
            statementRepository.save(statement);
        }

        mockMvc.perform(get("/api/v1/statements/{accountId}", accountId)
                        .param("page", "0")
                        .param("size", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(3))
                .andExpect(jsonPath("$.totalStatements").value(6))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.statements").isArray())
                .andExpect(jsonPath("$.statements.length()").value(3));
    }

    @Test
    @DisplayName("Deve retornar erro quando conta não existe")
    void shouldReturnErrorWhenAccountNotFound() throws Exception {
        Long nonExistentAccountId = 99999L;

        MvcResult result = mockMvc.perform(get("/api/v1/statements/{accountId}", nonExistentAccountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        ErrorResponse errorResponse = objectMapper.readValue(jsonResponse, ErrorResponse.class);

        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.message()).contains("Conta não encontrada");
    }

    @Test
    @DisplayName("Deve retornar extrato vazio para conta sem statements")
    void shouldReturnEmptyStatements() throws Exception {
        Account emptyAccount = new Account();
        emptyAccount.setName("Conta Vazia");
        emptyAccount.setType(PROPERTY_OWNER);
        emptyAccount.setBalance(BigDecimal.ZERO);
        emptyAccount = accountRepository.save(emptyAccount);

        mockMvc.perform(get("/api/v1/statements/{accountId}", emptyAccount.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(emptyAccount.getId()))
                .andExpect(jsonPath("$.totalStatements").value(0))
                .andExpect(jsonPath("$.statements").isEmpty());
    }

    @Test
    @DisplayName("Deve filtrar extrato por intervalo de datas")
    void shouldGetAccountStatementsByDateRange() throws Exception {
        Long accountId = realEstateAgencyAccount.getId();
        LocalDateTime startDate = LocalDateTime.now().minusDays(2);
        LocalDateTime endDate = LocalDateTime.now();

        mockMvc.perform(get("/api/v1/statements/{accountId}/filter", accountId)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.statements").isArray());
    }

    @Test
    @DisplayName("Deve filtrar extrato por data com paginação")
    void shouldGetAccountStatementsByDateRangeWithPagination() throws Exception {
        Long accountId = platformAccount.getId();
        
        // Criar mais statements no intervalo de datas
        for (int i = 1; i <= 10; i++) {
            Statement statement = new Statement();
            statement.setPaymentId(payment.getId());
            statement.setAccountId(platformAccount.getId());
            statement.setAmount(BigDecimal.valueOf(10.00 * i));
            statement.setDescription("Statement " + i);
            statement.setCreatedAt(LocalDateTime.now().minusHours(i));
            statementRepository.save(statement);
        }

        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        mockMvc.perform(get("/api/v1/statements/{accountId}/filter", accountId)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .param("page", "0")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(5))
                .andExpect(jsonPath("$.statements").isArray())
                .andExpect(jsonPath("$.statements.length()").value(5));
    }

    @Test
    @DisplayName("Deve retornar erro ao filtrar por data com conta inexistente")
    void shouldReturnErrorWhenFilteringByDateWithNonExistentAccount() throws Exception {
        Long nonExistentAccountId = 99999L;
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        MvcResult result = mockMvc.perform(get("/api/v1/statements/{accountId}/filter", nonExistentAccountId)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        ErrorResponse errorResponse = objectMapper.readValue(jsonResponse, ErrorResponse.class);

        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.message()).contains("Conta não encontrada");
    }

    @Test
    @DisplayName("Deve retornar extrato vazio quando não há statements no intervalo")
    void shouldReturnEmptyStatementsWhenNoResultsInDateRange() throws Exception {
        Long accountId = propertyOwnerAccount.getId();
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(7);

        mockMvc.perform(get("/api/v1/statements/{accountId}/filter", accountId)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.totalStatements").value(0))
                .andExpect(jsonPath("$.statements").isEmpty());
    }

    @Test
    @DisplayName("Deve usar valores padrão quando parâmetros de paginação não são fornecidos")
    void shouldUseDefaultPaginationParametersWhenNotProvided() throws Exception {
        Long accountId = propertyOwnerAccount.getId();

        mockMvc.perform(get("/api/v1/statements/{accountId}", accountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(20));
    }

    @Test
    @DisplayName("Deve ordenar statements por data de criação decrescente")
    void shouldOrderStatementsByCreatedAtDesc() throws Exception {
        Long accountId = platformAccount.getId();
        
        // Criar statements com datas específicas
        Statement oldStatement = new Statement();
        oldStatement.setPaymentId(payment.getId());
        oldStatement.setAccountId(platformAccount.getId());
        oldStatement.setAmount(BigDecimal.valueOf(10.00));
        oldStatement.setDescription("Statement Antigo");
        oldStatement.setCreatedAt(LocalDateTime.now().minusDays(5));
        statementRepository.save(oldStatement);

        Statement newStatement = new Statement();
        newStatement.setPaymentId(payment.getId());
        newStatement.setAccountId(platformAccount.getId());
        newStatement.setAmount(BigDecimal.valueOf(20.00));
        newStatement.setDescription("Statement Novo");
        newStatement.setCreatedAt(LocalDateTime.now().minusHours(1));
        statementRepository.save(newStatement);

        mockMvc.perform(get("/api/v1/statements/{accountId}", accountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statements[0].description").value("Statement Novo"))
                .andExpect(jsonPath("$.statements[1].description").value("Statement Antigo"));
    }
}