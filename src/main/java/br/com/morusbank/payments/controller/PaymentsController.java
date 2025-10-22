package br.com.morusbank.payments.controller;

import br.com.morusbank.payments.domain.exception.PaymentException;
import br.com.morusbank.payments.domain.request.PaymentRequest;
import br.com.morusbank.payments.domain.response.PaymentResponse;
import br.com.morusbank.payments.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentsController {

    private final PaymentService paymentService;

    public PaymentsController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) throws PaymentException {
        PaymentResponse paymentResponse = paymentService.processPayment(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(paymentResponse);
    }

}
