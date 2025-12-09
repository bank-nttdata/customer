package com.nttdata.bootcamp.service;


import com.nttdata.bootcamp.entity.Customer;
import reactor.core.publisher.Mono;

public interface RedisCacheService {

    /**
     * Recupera un Customer desde Redis de manera reactiva.
     */
    Mono<Customer> retrieveCustomer(String customerDni);

    /**
     * Elimina keys asociadas a un customer de manera reactiva.
     */
    Mono<Void> flushCustomerCache(String customerId);

    /**
     * Guarda un Customer en Redis con TTL de manera reactiva.
     */
    Mono<Customer> storeCustomer(String customerDni, Customer customer);

    /**
     * Limpia todo el Redis (flushAll) de forma reactiva.
     */
    Mono<Void> clearAll();

    //******************************
    Mono<Boolean> deleteCustomer(String customerId);


}
