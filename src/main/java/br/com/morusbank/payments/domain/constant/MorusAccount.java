package br.com.morusbank.payments.domain.constant;

import br.com.morusbank.payments.domain.entity.Account;

import java.math.BigDecimal;

public final class MorusAccount {
    public static final BigDecimal MOUROSBANK_FEE_PERCENTAGE = new BigDecimal("0.02");

    //Deixando a conta da morus static para facilitar o acesso e diminuir uma consulta no banco,
    // mas podemos usar um secret para isso ou um cache do banco aqui.
    public static Account getMorusAccount() {
        Account account = new Account();
        account.setId(1L);
        account.setName("Morus Bank");
        account.setBalance(new java.math.BigDecimal("0.02"));
        account.setType(Account.AccountType.PLATFORM_REVENUE);
        return account;
    }
}
