
package dev.pedrorodrigo.orderflow.domain;

import dev.pedrorodrigo.orderflow.domain.entity.Order;
import dev.pedrorodrigo.orderflow.domain.entity.Payment;
import dev.pedrorodrigo.orderflow.domain.enums.PaymentMethod;
import dev.pedrorodrigo.orderflow.domain.enums.PaymentStatus;
import dev.pedrorodrigo.orderflow.exception.InvalidPaymentStatusTransitionException;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentTest {

    private Payment createPayment(LocalDateTime expiresAt) {
        Order order = mock(Order.class);
        when(order.getTotalAmount())
                .thenReturn(new BigDecimal("150.00"));

        return new Payment(
                order,
                PaymentMethod.PIX,
                1,
                expiresAt
        );
    }

    private Payment pendingPayment() {
        return createPayment(LocalDateTime.now().plusHours(1));
    }

    @Test
    void shouldCreatePendingPayment() {
        Payment payment = pendingPayment();

        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertEquals(0, new BigDecimal("150.00")
                .compareTo(payment.getAmount()));
    }

    @Test
    void shouldCompletePendingPayment() {
        Payment payment = pendingPayment();

        payment.complete();

        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
    }

    @Test
    void shouldFailPendingPayment() {
        Payment payment = pendingPayment();

        payment.fail();

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
    }

    @Test
    void shouldCancelPendingPayment() {
        Payment payment = pendingPayment();

        payment.cancel();

        assertEquals(PaymentStatus.CANCELLED, payment.getStatus());
    }

    @Test
    void shouldNotCompleteFailedPayment() {
        Payment payment = pendingPayment();
        payment.fail();

        assertThrows(
                InvalidPaymentStatusTransitionException.class,
                payment::complete
        );

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
    }

    @Test
    void shouldNotCompleteCancelledPayment() {
        Payment payment = pendingPayment();
        payment.cancel();

        assertThrows(
                InvalidPaymentStatusTransitionException.class,
                payment::complete
        );
    }

    @Test
    void shouldNotCompletePaymentTwice() {
        Payment payment = pendingPayment();
        payment.complete();

        assertThrows(
                InvalidPaymentStatusTransitionException.class,
                payment::complete
        );
    }

    @Test
    void shouldNotFailCompletedPayment() {
        Payment payment = pendingPayment();
        payment.complete();

        assertThrows(
                InvalidPaymentStatusTransitionException.class,
                payment::fail
        );
    }

    @Test
    void shouldNotCancelCompletedPayment() {
        Payment payment = pendingPayment();
        payment.complete();

        assertThrows(
                InvalidPaymentStatusTransitionException.class,
                payment::cancel
        );
    }

    @Test
    void shouldIdentifyExpiredPayment() throws Exception {
        Payment payment = pendingPayment();

        Field expiresAtField = Payment.class
                .getDeclaredField("expiresAt");

        expiresAtField.setAccessible(true);

        expiresAtField.set(
                payment,
                LocalDateTime.now().minusMinutes(1)
        );

        boolean expired = payment.isExpired();

        assertTrue(expired);
    }

    @Test
    void shouldRejectPastExpirationOnCreation() {
        Order order = mock(Order.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> new Payment(
                        order,
                        PaymentMethod.PIX,
                        1,
                        LocalDateTime.now().minusMinutes(1)
                )
        );
    }

    @Test
    void shouldRejectZeroInstallments() {
        Order order = mock(Order.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> new Payment(
                        order,
                        PaymentMethod.CREDIT_CARD,
                        0,
                        LocalDateTime.now().plusHours(1)
                )
        );
    }
}
