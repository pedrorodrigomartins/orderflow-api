package dev.pedrorodrigo.orderflow.controller;

import dev.pedrorodrigo.orderflow.dto.request.CreatePaymentRequest;
import dev.pedrorodrigo.orderflow.dto.response.PaymentResponse;
import dev.pedrorodrigo.orderflow.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(@RequestBody @Valid CreatePaymentRequest request) {
        return paymentService.createPayment(request);
    }

    @PatchMapping("/{id}/complete")
    public PaymentResponse completePayment(@PathVariable Long id) {
        return paymentService.completePayment(id);
    }

    @PatchMapping("/{id}/fail")
    public PaymentResponse failPayment(@PathVariable Long id) {
        return paymentService.failPayment(id);
    }

    @GetMapping("/{id}")
    public PaymentResponse getPaymentById(@PathVariable Long id) {
        return paymentService.getPaymentById(id);
    }

    @GetMapping("/order/{orderId}")
    public List<PaymentResponse> getPaymentsByOrderId(
            @PathVariable Long orderId
    ) {
        return paymentService.getPaymentsByOrderId(orderId);
    }
}
