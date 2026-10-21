
package dev.pedrorodrigo.orderflow.integration;

import dev.pedrorodrigo.orderflow.domain.entity.Category;
import dev.pedrorodrigo.orderflow.domain.entity.Customer;
import dev.pedrorodrigo.orderflow.domain.entity.Product;
import dev.pedrorodrigo.orderflow.dto.request.CreateOrderItemRequest;
import dev.pedrorodrigo.orderflow.dto.request.CreateOrderRequest;
import dev.pedrorodrigo.orderflow.exception.InsufficientStockException;
import dev.pedrorodrigo.orderflow.repository.CategoryRepository;
import dev.pedrorodrigo.orderflow.repository.CustomerRepository;
import dev.pedrorodrigo.orderflow.repository.OrderRepository;
import dev.pedrorodrigo.orderflow.repository.ProductRepository;
import dev.pedrorodrigo.orderflow.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

class OrderRollbackIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldRollbackStockChangesWhenOrderFails() {

        String suffix = UUID.randomUUID().toString().substring(0, 8);

        String cpf = String.format(
                "%011d",
                ThreadLocalRandom.current()
                        .nextLong(10000000000L, 99999999999L)
        );

        Customer customer = customerRepository.save(
                new Customer(
                        "Rollback Customer " + suffix,
                        cpf,
                        "rollback-" + suffix + "@test.com"
                )
        );

        Category category = categoryRepository.save(
                new Category(
                        "Rollback Category " + suffix,
                        "Category used in rollback tests"
                )
        );

        Product firstProduct = productRepository.save(
                new Product(
                        "First Product",
                        "FIRST-" + suffix,
                        10,
                        "Available product",
                        new BigDecimal("50.00"),
                        category
                )
        );

        Product secondProduct = productRepository.save(
                new Product(
                        "Second Product",
                        "SECOND-" + suffix,
                        0,
                        "Out of stock",
                        new BigDecimal("75.00"),
                        category
                )
        );

        CreateOrderRequest request = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new CreateOrderItemRequest(firstProduct.getId(), 3),
                        new CreateOrderItemRequest(secondProduct.getId(), 1)
                )
        );

        long ordersBefore = orderRepository.count();

        assertThrows(
                InsufficientStockException.class,
                () -> orderService.createOrder(request)
        );

        Product persistedFirst = productRepository
                .findById(firstProduct.getId())
                .orElseThrow();

        Product persistedSecond = productRepository
                .findById(secondProduct.getId())
                .orElseThrow();

        assertEquals(10, persistedFirst.getStockQuantity());
        assertEquals(0, persistedSecond.getStockQuantity());

        assertEquals(ordersBefore, orderRepository.count());
    }
}
