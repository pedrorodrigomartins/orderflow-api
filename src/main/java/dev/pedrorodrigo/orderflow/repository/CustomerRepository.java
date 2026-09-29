package dev.pedrorodrigo.orderflow.repository;

import dev.pedrorodrigo.orderflow.domain.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCpf(String cpf);
    Optional<Customer> findByEmail(String email);

    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);

}
