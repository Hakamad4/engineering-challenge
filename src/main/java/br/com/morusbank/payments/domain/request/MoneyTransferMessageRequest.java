package br.com.morusbank.payments.domain.request;

import br.com.morusbank.payments.domain.entity.Account;
import br.com.morusbank.payments.domain.entity.Statement;

public record MoneyTransferMessageRequest(Account account, Statement statement) {

}
