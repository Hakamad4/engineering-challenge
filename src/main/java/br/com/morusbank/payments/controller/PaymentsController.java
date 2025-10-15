package br.com.morusbank.payments.controller;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentsController {

    // Here you would define endpoints to handle payment processing. For example:
    // @PostMapping("/payments")
    // public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
    //     // Payment processing logic goes here
    //     return ResponseEntity.ok(new PaymentResponse(...));
    // }

    // Optional endpoints (e.g. GET payment status, GET account balances) can live in separate controllers.
    // They are not required for core functionality; primary focus is the domain model (accounts, statement, payments).

}
