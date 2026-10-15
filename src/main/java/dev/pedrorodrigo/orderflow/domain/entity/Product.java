package dev.pedrorodrigo.orderflow.domain.entity;

import dev.pedrorodrigo.orderflow.exception.InsufficientStockException;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(length = 100, nullable = false)
    private String name;

    @Setter
    @Column(length = 50, nullable = false, unique = true)
    private String sku;

    @Setter
    @PositiveOrZero
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Setter
    @Column(columnDefinition = "TEXT")
    private String description;

    @Setter
    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean active = true;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Product(
            String name,
            String sku,
            Integer stockQuantity,
            String description,
            BigDecimal price,
            Category category
    ) {
        this.name = name;
        this.sku = sku;
        this.stockQuantity = stockQuantity;
        this.description = description;
        this.price = price;
        this.category = category;
    }

    public void changeActiveStatus(boolean active) {
        this.active = active;
    }

    public void decreaseStock(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero."
            );
        }

        if (stockQuantity < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for product with id '" + id + "'."
            );
        }

        this.stockQuantity -= quantity;
    }

    public void increaseStock(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero."
            );
        }

        this.stockQuantity += quantity;
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
