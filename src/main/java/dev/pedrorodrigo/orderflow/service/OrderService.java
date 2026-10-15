
package dev.pedrorodrigo.orderflow.service;

import dev.pedrorodrigo.orderflow.domain.entity.Customer;
import dev.pedrorodrigo.orderflow.domain.entity.Order;
import dev.pedrorodrigo.orderflow.domain.entity.OrderItem;
import dev.pedrorodrigo.orderflow.domain.entity.Product;
import dev.pedrorodrigo.orderflow.domain.enums.OrderStatus;
import dev.pedrorodrigo.orderflow.dto.request.CreateOrderItemRequest;
import dev.pedrorodrigo.orderflow.dto.request.CreateOrderRequest;
import dev.pedrorodrigo.orderflow.dto.response.OrderItemResponse;
import dev.pedrorodrigo.orderflow.dto.response.OrderResponse;
import dev.pedrorodrigo.orderflow.exception.*;
import dev.pedrorodrigo.orderflow.repository.CustomerRepository;
import dev.pedrorodrigo.orderflow.repository.OrderRepository;
import dev.pedrorodrigo.orderflow.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException(
                    "Order must contain at least one item."
            );
        }

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer with id '" + request.customerId() + "' not found."
                ));

        Set<Long> productIds = new HashSet<>();

        for (CreateOrderItemRequest item : request.items()) {

            if (item == null || item.productId() == null
                    || item.quantity() == null || item.quantity() <= 0) {
                throw new IllegalArgumentException(
                        "Order item must have a product ID and a positive quantity."
                );
            }

            if (!productIds.add(item.productId())) {
                throw new DuplicateOrderItemException(
                        "Product with id '" + item.productId() + "' is duplicated."
                );
            }
        }

        List<CreateOrderItemRequest> sortedItems = request.items()
                .stream()
                .sorted(Comparator.comparing(CreateOrderItemRequest::productId))
                .toList();

        Order order = new Order(customer);

        for (CreateOrderItemRequest item : sortedItems) {

            Product product = productRepository.findByIdForUpdate(item.productId())
                    .orElseThrow(() -> new ProductNotFoundException(
                            "Product with id '" + item.productId() + "' not found."
                    ));

            if (!product.isActive()) {
                throw new ProductInactiveException(
                        "Product with id '" + product.getId() + "' is inactive."
                );
            }

            product.decreaseStock(item.quantity());

            OrderItem orderItem = new OrderItem(
                    product,
                    item.quantity(),
                    product.getPrice()
            );

            order.addItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        return toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order with id '" + orderId + "' not found."
                ));
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomerId(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        return orders.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order with id '" + id + "' not found."
                ));

        order.changeStatus(status);

        return toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order with id '" + id + "' not found."
                ));

        order.cancel();

        List<OrderItem> sortedItems = order.getItems()
                .stream()
                .sorted(Comparator.comparing(item -> item.getProduct().getId()))
                .toList();

        for (OrderItem item : sortedItems) {
            Long productId = item.getProduct().getId();

            Product product = productRepository.findByIdForUpdate(productId)
                    .orElseThrow(() -> new ProductNotFoundException(
                            "Product with id '" + productId + "' not found."
                    ));

            product.increaseStock(item.getQuantity());
        }

        return toResponse(order);
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal(),
                item.getCreatedAt()
        );
    }

    private OrderResponse toResponse(Order order) {

        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCustomer().getId(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
