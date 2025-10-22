package br.com.morusbank.payments.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "property_owners")
public class PropertyOwner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "real_estate_agency_id")
    private Long realEstateAgencyId;

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

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getRealEstateAgencyId() {
        return realEstateAgencyId;
    }

    public void setRealEstateAgencyId(Long realEstateAgencyId) {
        this.realEstateAgencyId = realEstateAgencyId;
    }
}
