package dev.pedrorodrigo.orderflow.repository;

import dev.pedrorodrigo.orderflow.domain.entity.Payment;
import dev.pedrorodrigo.orderflow.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByOrderId(Long orderId);
    List<Payment> findByOrderIdAndStatus(Long orderId, String status);

    boolean existsByOrderIdAndStatusIn(
            Long orderId,
            Collection<PaymentStatus> status
    );

}
