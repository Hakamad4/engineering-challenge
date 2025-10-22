package br.com.morusbank.payments.integration;

import br.com.morusbank.payments.domain.entity.Payment;
import br.com.morusbank.payments.domain.entity.Statement;
import br.com.morusbank.payments.domain.repository.AccountRepository;
import br.com.morusbank.payments.domain.repository.PaymentRepository;
import br.com.morusbank.payments.domain.repository.StatementRepository;
import br.com.morusbank.payments.domain.request.PaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@DisplayName("Testes de Integração - Payments API")
public class PaymentsIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private StatementRepository statementRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1";
    }

    @Test
    @DisplayName("Deve processar pagamento via API com sucesso")
    void shouldProcessPaymentViaApiSuccessfully() {
        PaymentRequest request = new PaymentRequest(
                1L, 1L, "PAY-INTEGRATION-NEW", new BigDecimal("3000.00"), "Test"
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PaymentRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/payments", entity, String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(paymentRepository.findByExternalReference("PAY-INTEGRATION-NEW").isPresent());

        Payment payment = paymentRepository.findByExternalReference("PAY-INTEGRATION-NEW").get();
        List<Statement> statements = statementRepository.findByPaymentId(payment.getId());
        assertEquals(3, statements.size());
    }

}
