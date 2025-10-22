package br.com.morusbank.payments.domain.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    public enum Status {PENDING, COMPLETED, FAILED}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_reference")
    private String externalReference;

    @Column(name = "real_estate_agency_id")
    private Long realEstateAgencyId;

    @Column(name = "property_owner_id")
    private Long propertyOwnerId;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
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
