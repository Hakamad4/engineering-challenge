package br.com.morusbank.payments.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {

    public enum Status {PENDING, COMPLETED, FAILED}

    private Long id;
    private String externalReference;
    private Long realEstateAgencyId;
    private Long propertyOwnerId;
    private BigDecimal amount;
    private Status status;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public Long getRealEstateAgencyId() {
        return realEstateAgencyId;
    }

    public void setRealEstateAgencyId(Long realEstateAgencyId) {
        this.realEstateAgencyId = realEstateAgencyId;
    }

    public Long getPropertyOwnerId() {
        return propertyOwnerId;
    }

    public void setPropertyOwnerId(Long propertyOwnerId) {
        this.propertyOwnerId = propertyOwnerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
