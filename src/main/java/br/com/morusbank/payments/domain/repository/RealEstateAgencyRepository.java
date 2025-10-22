package br.com.morusbank.payments.domain.repository;

import br.com.morusbank.payments.domain.entity.PropertyOwner;
import br.com.morusbank.payments.domain.entity.RealEstateAgency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RealEstateAgencyRepository extends JpaRepository<RealEstateAgency, Long> {



}
