package br.com.morusbank.payments.util;

import br.com.morusbank.payments.domain.Account;
import br.com.morusbank.payments.domain.PropertyOwner;
import br.com.morusbank.payments.domain.RealEstateAgency;

import java.math.BigDecimal;
import java.util.List;

/**
 * Utility class to create default fixture data for testing purposes.
 */
public class FixtureHelper {

    public static List<Account> createDefaultAccounts() {
        return List.of(
                createAccount(2L, "Morus Receita", Account.AccountType.PLATFORM_REVENUE, new BigDecimal("0.00")),
                createAccount(3L, "Imobiliária Alfa", Account.AccountType.REAL_ESTATE_AGENCY, new BigDecimal("0.00")),
                createAccount(4L, "João da Silva (Proprietário)", Account.AccountType.PROPERTY_OWNER, new BigDecimal("0.00"))
        );
    }

    public static RealEstateAgency createDefaultAgency() {
        var agency = new RealEstateAgency();
        agency.setId(10L);
        agency.setName("Imobiliária Alfa");
        agency.setAccountId(3L);
        agency.setFeePercentage(new BigDecimal("10")); // 10%
        return agency;
    }

    public static PropertyOwner createDefaultOwner() {
        var owner = new PropertyOwner();
        owner.setId(20L);
        owner.setName("João da Silva");
        owner.setRealEstateAgencyId(10L);
        owner.setAccountId(4L);
        return owner;
    }

    private static Account createAccount(Long id, String name, Account.AccountType type, BigDecimal balance) {
        var a = new Account();
        a.setId(id);
        a.setName(name);
        a.setType(type);
        a.setBalance(balance);
        return a;
    }
}
