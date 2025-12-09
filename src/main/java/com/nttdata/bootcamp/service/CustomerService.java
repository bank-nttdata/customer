package com.nttdata.bootcamp.service;

import com.nttdata.bootcamp.entity.Customer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

//Interface Service
public interface CustomerService {

    Mono<Customer> save(Customer customer);
    Mono<Customer> updateCustomerAddress(Customer dataCustomer);
    Mono<Customer> updateStatus(Customer dataCustomer);
    Mono<Void> delete(String dni);
    Customer saveInitServices(Customer dataCustomer);
    Flux<Customer> findAll();
    Mono<Customer> findByDni(String dni);


}
