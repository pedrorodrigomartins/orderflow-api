package dev.pedrorodrigo.orderflow.service;

import dev.pedrorodrigo.orderflow.domain.entity.Order;
import dev.pedrorodrigo.orderflow.domain.entity.Payment;
import dev.pedrorodrigo.orderflow.domain.enums.OrderStatus;
import dev.pedrorodrigo.orderflow.domain.enums.PaymentStatus;
import dev.pedrorodrigo.orderflow.dto.request.CreatePaymentRequest;
import dev.pedrorodrigo.orderflow.dto.response.PaymentResponse;
import dev.pedrorodrigo.orderflow.exception.OrderNotFoundException;
import dev.pedrorodrigo.orderflow.exception.PaymentAlreadyExistsException;
import dev.pedrorodrigo.orderflow.exception.PaymentNotFoundException;
import dev.pedrorodrigo.orderflow.repository.OrderRepository;
import dev.pedrorodrigo.orderflow.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        Order order = orderRepository.findByIdForUpdate(request.orderId())
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order with id '" + request.orderId() + "' not found."
                ));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only pending orders can be paid."
            );
        }

        boolean paymentExists = paymentRepository.existsByOrderIdAndStatusIn(
                order.getId(),
                List.of(PaymentStatus.PENDING, PaymentStatus.COMPLETED)
        );

        if (paymentExists) {
            throw new PaymentAlreadyExistsException(
                    "Order with id '" + order.getId()
                            + "' already has a pending or completed payment."
            );
        }

        Payment payment = new Payment(
                order,
                request.paymentMethod(),
                request.installments(),
                request.expiresAt()
        );

        Payment savedPayment = paymentRepository.save(payment);

        return toResponse(savedPayment);
    }

    @Transactional
    public PaymentResponse completePayment(Long paymentId) {
        Long orderId = paymentRepository.findOrderIdByPaymentId(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment with id '" + paymentId + "' not found."
                ));

        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order with id '" + orderId + "' not found."
                ));

        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment with id '" + paymentId + "' not found."
                ));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending orders can have payments completed."
            );
        }

        payment.complete();

        order.changeStatus(OrderStatus.PAID);

        return toResponse(payment);
    }

    @Transactional
    public PaymentResponse failPayment(Long paymentId) {
        Long orderId = paymentRepository.findOrderIdByPaymentId(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment with id '" + paymentId + "' not found."
                ));

        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order with id '" + orderId + "' not found."
                ));

        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment with id '" + paymentId + "' not found."
                ));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Only payments from pending orders can be failed."
            );
        }

        payment.fail();

        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getInstallments(),
                payment.getExpiresAt(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
