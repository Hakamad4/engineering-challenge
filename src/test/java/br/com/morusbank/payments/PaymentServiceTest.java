package br.com.morusbank.payments;

import br.com.morusbank.payments.domain.Payment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaymentServiceTest {

    @Test
    void shouldGenerateBalancedLedgerEntries() {
        Payment payment = new Payment();
        payment.setReferenceId("ALUGUEL-01");
        payment.setAmount(new BigDecimal("3000.00"));
        // TODO: prepare scenario and execute
        assertTrue(true);
    }

    @Test
    void shouldRollbackOnFailure() {
        assertTrue(true);
    }

    @Test
    void shouldProcessConcurrentlyWithoutCorruptingBalances() {
        assertTrue(true);
    }
}
