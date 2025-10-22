package br.com.morusbank.payments.domain.repository;

import br.com.morusbank.payments.domain.entity.Payment;
import br.com.morusbank.payments.domain.entity.PropertyOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropertyOwnerRepository extends JpaRepository<PropertyOwner, Long> {

}
