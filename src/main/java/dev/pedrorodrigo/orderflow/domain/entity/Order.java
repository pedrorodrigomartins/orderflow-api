
package dev.pedrorodrigo.orderflow.domain.entity;

import dev.pedrorodrigo.orderflow.domain.enums.OrderStatus;
import dev.pedrorodrigo.orderflow.exception.InvalidOrderCancellationException;
import dev.pedrorodrigo.orderflow.exception.InvalidOrderStatusTransitionException;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Positive
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Order(Customer customer) {
        this.customer = customer;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.assignOrder(this);
        totalAmount = totalAmount.add(item.getSubtotal());
    }

    public void changeStatus(OrderStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "Order status cannot be null."
            );
        }

        boolean validTransition = switch (this.status) {
            case PENDING -> newStatus == OrderStatus.PAID;
            case PAID -> newStatus == OrderStatus.SHIPPED;
            case SHIPPED -> newStatus == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };

        if (!validTransition) {
            throw new InvalidOrderStatusTransitionException(
                    "Cannot change order status from "
                            + this.status + " to " + newStatus
            );
        }

        this.status = newStatus;
    }

    public void cancel() {
        if (this.status != OrderStatus.PENDING) {
            throw new InvalidOrderCancellationException(
                    "Only pending orders can be cancelled."
            );
        }

        this.status = OrderStatus.CANCELLED;
    }

    @PrePersist
    private void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
