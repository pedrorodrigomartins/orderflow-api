package dev.pedrorodrigo.orderflow.repository;

import dev.pedrorodrigo.orderflow.domain.entity.Payment;
import dev.pedrorodrigo.orderflow.domain.enums.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByOrderId(Long orderId);
    List<Payment> findByOrderIdAndStatus(
            Long orderId,
            PaymentStatus status
    );

    @Query("SELECT p.order.id FROM Payment p WHERE p.id = :id")
    Optional<Long> findOrderIdByPaymentId(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.id = :id")
    Optional<Payment> findByIdForUpdate(@Param("id") Long id);

    List<Payment> findByOrderIdAndStatusOrderByIdAsc(
            Long orderId,
            PaymentStatus status
    );

}
