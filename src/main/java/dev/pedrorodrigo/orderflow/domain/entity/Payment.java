package dev.pedrorodrigo.orderflow.domain.entity;

import dev.pedrorodrigo.orderflow.domain.enums.PaymentMethod;
import dev.pedrorodrigo.orderflow.domain.enums.PaymentStatus;
import dev.pedrorodrigo.orderflow.exception.InvalidPaymentStatusTransitionException;
import dev.pedrorodrigo.orderflow.exception.PaymentExpiredException;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 50)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Positive
    @Column(nullable = false)
    private Integer installments;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Payment(
            Order order,
            PaymentMethod paymentMethod,
            Integer installments,
            LocalDateTime expiresAt
    ) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null.");
        }

        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method cannot be null.");
        }

        if (installments == null || installments <= 0) {
            throw new IllegalArgumentException(
                    "Installments must be greater than zero."
            );
        }

        if (expiresAt == null || !expiresAt.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Expiration date must be in the future."
            );
        }

        if (expiresAt == null || !expiresAt.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Payment expiration date must be in the future."
            );
        }

        this.order = order;
        this.amount = order.getTotalAmount();
        this.paymentMethod = paymentMethod;
        this.status = PaymentStatus.PENDING;
        this.installments = installments;
        this.expiresAt = expiresAt;
    }

    public void complete() {
        if (this.status != PaymentStatus.PENDING) {
            throw new InvalidPaymentStatusTransitionException(
                    "Only pending payments can be completed. Current status: "
                            + this.status
            );
        }
        if (!this.expiresAt.isAfter(LocalDateTime.now())) {
            throw new PaymentExpiredException(
                    "Payment with id '" + this.id + "' has expired."
            );
        }

        this.status = PaymentStatus.COMPLETED;
    }

    public void fail() {
        if (this.status != PaymentStatus.PENDING) {
            throw new InvalidPaymentStatusTransitionException(
                    "Only pending payments can be failed. Current status: "
                            + this.status
            );
        }

        this.status = PaymentStatus.FAILED;
    }

    public boolean isExpired() {
        return !expiresAt.isAfter(LocalDateTime.now());
    }

    public void cancel() {
        if (this.status != PaymentStatus.PENDING) {
            throw new InvalidPaymentStatusTransitionException(
                    "Only pending payments can be cancelled."
            );
        }

        this.status = PaymentStatus.CANCELLED;
    }

    @PrePersist
    private void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
