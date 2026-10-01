package dev.pedrorodrigo.orderflow.service;

import dev.pedrorodrigo.orderflow.domain.entity.Customer;
import dev.pedrorodrigo.orderflow.dto.CreateCustomerRequest;
import dev.pedrorodrigo.orderflow.dto.CustomerResponse;
import dev.pedrorodrigo.orderflow.exception.ExistingCustomerException;
import dev.pedrorodrigo.orderflow.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {

        if (customerRepository.existsByCpf(request.cpf())) {
            throw new ExistingCustomerException(
                    "Customer with CPF " + request.cpf() + " already exists"
            );
        }

        if (customerRepository.existsByEmail(request.email())) {
            throw new ExistingCustomerException(
                    "Customer with email " + request.email() + " already exists"
            );
        }

        Customer customer = new Customer(
                request.name(),
                request.cpf(),
                request.email()
        );

        Customer savedCustomer = customerRepository.save(customer);

        return new CustomerResponse(
                savedCustomer.getId(),
                savedCustomer.getName(),
                savedCustomer.getEmail(),
                savedCustomer.getCreatedAt()
        );
    }
}