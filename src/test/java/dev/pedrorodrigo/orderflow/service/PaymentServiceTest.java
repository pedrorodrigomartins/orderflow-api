
package dev.pedrorodrigo.orderflow.service;

import dev.pedrorodrigo.orderflow.domain.entity.Order;
import dev.pedrorodrigo.orderflow.domain.entity.Payment;
import dev.pedrorodrigo.orderflow.domain.enums.OrderStatus;
import dev.pedrorodrigo.orderflow.domain.enums.PaymentMethod;
import dev.pedrorodrigo.orderflow.domain.enums.PaymentStatus;
import dev.pedrorodrigo.orderflow.dto.request.CreatePaymentRequest;
import dev.pedrorodrigo.orderflow.dto.response.PaymentResponse;
import dev.pedrorodrigo.orderflow.exception.PaymentAlreadyExistsException;
import dev.pedrorodrigo.orderflow.exception.PaymentNotFoundException;
import dev.pedrorodrigo.orderflow.repository.OrderRepository;
import dev.pedrorodrigo.orderflow.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    private PaymentRepository paymentRepository;
    private OrderRepository orderRepository;
    private PaymentService paymentService;

    private Order order;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        orderRepository = mock(OrderRepository.class);

        paymentService = new PaymentService(
                paymentRepository,
                orderRepository
        );

        order = mock(Order.class);

        when(order.getId()).thenReturn(1L);
        when(order.getStatus()).thenReturn(OrderStatus.PENDING);
        when(order.getTotalAmount())
                .thenReturn(new BigDecimal("150.00"));
    }

    private CreatePaymentRequest request() {
        return new CreatePaymentRequest(
                1L,
                PaymentMethod.PIX,
                1,
                LocalDateTime.now().plusHours(1)
        );
    }

    private Payment pendingPayment() {
        return new Payment(
                order,
                PaymentMethod.PIX,
                1,
                LocalDateTime.now().plusHours(1)
        );
    }

    @Test
    void shouldCreatePayment() {
        when(orderRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(List.of());

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response =
                paymentService.createPayment(request());

        assertEquals(PaymentStatus.PENDING, response.status());
        assertEquals(1L, response.orderId());

        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void shouldRejectDuplicatePendingPayment() {
        when(orderRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(order));

        Payment existingPayment = pendingPayment();

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(List.of(existingPayment));

        assertThrows(
                PaymentAlreadyExistsException.class,
                () -> paymentService.createPayment(request())
        );

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }

    @Test
    void shouldAllowRetryAfterFailedPayment() {
        Payment previous = pendingPayment();
        previous.fail();

        when(orderRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(List.of(previous));

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response =
                paymentService.createPayment(request());

        assertEquals(PaymentStatus.PENDING, response.status());
        assertEquals(PaymentStatus.FAILED, previous.getStatus());
    }

    @Test
    void shouldRejectPaymentForPaidOrder() {
        when(order.getStatus()).thenReturn(OrderStatus.PAID);

        when(orderRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.createPayment(request())
        );

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }

    @Test
    void shouldReturnNotFoundForUnknownPayment() {
        when(paymentRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                PaymentNotFoundException.class,
                () -> paymentService.getPaymentById(999L)
        );
    }

    @Test
    void shouldRejectSecondPaymentAfterCompletedPayment() {
        Payment previous = pendingPayment();
        previous.complete();

        when(orderRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(List.of(previous));

        assertThrows(
                PaymentAlreadyExistsException.class,
                () -> paymentService.createPayment(request())
        );

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }
}
