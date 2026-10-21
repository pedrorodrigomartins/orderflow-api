
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class OrderConcurrencyIntegrationTest extends AbstractIntegrationTest {

    private static final AtomicLong CPF_SEQUENCE =
            new AtomicLong(10000000000L);

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
    void shouldPreventOversellingWhenTwoOrdersCompete() throws Exception {

        String suffix = UUID.randomUUID().toString().substring(0, 8);

        Customer customer = customerRepository.save(
                new Customer(
                        "Customer " + suffix,
                        String.valueOf(CPF_SEQUENCE.getAndIncrement()),
                        "customer-" + suffix + "@test.com"
                )
        );

        Category category = categoryRepository.save(
                new Category(
                        "Category " + suffix,
                        "Category used in concurrency tests"
                )
        );

        Product product = productRepository.save(
                new Product(
                        "Limited Product",
                        "SKU-" + suffix,
                        1,
                        "Product with only one unit",
                        new BigDecimal("100.00"),
                        category
                )
        );

        CreateOrderRequest request = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new CreateOrderItemRequest(product.getId(), 1)
                )
        );

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Callable<Boolean> task = () -> {
                ready.countDown();

                if (!start.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException(
                            "Concurrent test did not start."
                    );
                }

                try {
                    orderService.createOrder(request);
                    return true;
                } catch (InsufficientStockException exception) {
                    return false;
                }
            };

            Future<Boolean> first = executor.submit(task);
            Future<Boolean> second = executor.submit(task);

            assertTrue(ready.await(10, TimeUnit.SECONDS));

            start.countDown();

            boolean firstSucceeded = first.get(20, TimeUnit.SECONDS);
            boolean secondSucceeded = second.get(20, TimeUnit.SECONDS);

            int successfulOrders =
                    (firstSucceeded ? 1 : 0)
                            + (secondSucceeded ? 1 : 0);

            assertEquals(1, successfulOrders);

            Product persistedProduct = productRepository
                    .findById(product.getId())
                    .orElseThrow();

            assertEquals(0, persistedProduct.getStockQuantity());

            assertEquals(
                    1,
                    orderRepository.findByCustomerId(customer.getId()).size()
            );

        } finally {
            executor.shutdownNow();
        }
    }
}
