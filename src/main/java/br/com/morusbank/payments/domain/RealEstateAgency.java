
package br.com.morusbank.payments.domain;

import java.math.BigDecimal;

public class RealEstateAgency {
    private Long id;
    private String name;
    private Long accountId;
    private BigDecimal feePercentage;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public BigDecimal getFeePercentage() { return feePercentage; }
    public void setFeePercentage(BigDecimal feePercentage) { this.feePercentage = feePercentage; }
}
