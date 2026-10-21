
package dev.pedrorodrigo.orderflow.domain;

import dev.pedrorodrigo.orderflow.domain.entity.Customer;
import dev.pedrorodrigo.orderflow.domain.entity.Order;
import dev.pedrorodrigo.orderflow.domain.enums.OrderStatus;
import dev.pedrorodrigo.orderflow.exception.InvalidOrderCancellationException;
import dev.pedrorodrigo.orderflow.exception.InvalidOrderStatusTransitionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class OrderTest {

    private Order newOrder() {
        return new Order(mock(Customer.class));
    }

    @Test
    void shouldCreatePendingOrder() {
        Order order = newOrder();

        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void shouldCancelPendingOrder() {
        Order order = newOrder();

        order.cancel();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void shouldNotCancelPaidOrder() {
        Order order = newOrder();
        order.changeStatus(OrderStatus.PAID);

        assertThrows(
                InvalidOrderCancellationException.class,
                order::cancel
        );
    }

    @Test
    void shouldNotCancelOrderTwice() {
        Order order = newOrder();
        order.cancel();

        assertThrows(
                InvalidOrderCancellationException.class,
                order::cancel
        );
    }

    @Test
    void shouldTransitionFromPendingToPaid() {
        Order order = newOrder();

        order.changeStatus(OrderStatus.PAID);

        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    void shouldTransitionFromPaidToShipped() {
        Order order = newOrder();
        order.changeStatus(OrderStatus.PAID);

        order.changeStatus(OrderStatus.SHIPPED);

        assertEquals(OrderStatus.SHIPPED, order.getStatus());
    }

    @Test
    void shouldTransitionFromShippedToDelivered() {
        Order order = newOrder();
        order.changeStatus(OrderStatus.PAID);
        order.changeStatus(OrderStatus.SHIPPED);

        order.changeStatus(OrderStatus.DELIVERED);

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void shouldNotShipPendingOrder() {
        Order order = newOrder();

        assertThrows(
                InvalidOrderStatusTransitionException.class,
                () -> order.changeStatus(OrderStatus.SHIPPED)
        );
    }

    @Test
    void shouldNotModifyDeliveredOrder() {
        Order order = newOrder();
        order.changeStatus(OrderStatus.PAID);
        order.changeStatus(OrderStatus.SHIPPED);
        order.changeStatus(OrderStatus.DELIVERED);

        assertThrows(
                InvalidOrderStatusTransitionException.class,
                () -> order.changeStatus(OrderStatus.PAID)
        );
    }

    @Test
    void shouldNotModifyCancelledOrder() {
        Order order = newOrder();
        order.cancel();

        assertThrows(
                InvalidOrderStatusTransitionException.class,
                () -> order.changeStatus(OrderStatus.PAID)
        );
    }

    @Test
    void shouldRejectNullStatus() {
        Order order = newOrder();

        assertThrows(
                IllegalArgumentException.class,
                () -> order.changeStatus(null)
        );
    }
}
