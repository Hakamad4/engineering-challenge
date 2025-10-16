package br.com.morusbank.payments.domain;

import java.math.BigDecimal;

public class Account {

    public enum AccountType {PLATFORM_REVENUE, REAL_ESTATE_AGENCY, PROPERTY_OWNER}

    private Long id;
    private String name;
    private BigDecimal balance;
    private AccountType type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }
}
