package br.com.morusbank.payments.domain.repository;

import br.com.morusbank.payments.domain.entity.Statement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatementRepository extends JpaRepository<Statement, Long> {

    List<Statement> findByPaymentId(Long paymentId);

    List<Statement> findByAccountId(Long accountId);

    Page<Statement> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);

    @Query("SELECT s FROM Statement s WHERE s.accountId = :accountId " +
            "AND s.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY s.createdAt DESC")
    Page<Statement> findByAccountIdAndDateRange(
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // Contar statements de uma conta
    long countByAccountId(Long accountId);
}
