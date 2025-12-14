package com.nttdata.bootcamp.repository;

import com.nttdata.bootcamp.entity.Customer;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

//Mongodb Repository
public interface CustomerRepository extends ReactiveCrudRepository<Customer, String> {

    Mono<Boolean> existsByDni(String dni);
    Mono<Customer> findByDni(String dni);
    Mono<Boolean> existsByRuc(String ruc);

}
