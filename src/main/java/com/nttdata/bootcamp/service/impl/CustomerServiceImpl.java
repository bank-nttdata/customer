package com.nttdata.bootcamp.service.impl;

import com.nttdata.bootcamp.controller.CustomerController;
import com.nttdata.bootcamp.entity.Customer;
import com.nttdata.bootcamp.entity.dto.CustomerCreateResponse;
import com.nttdata.bootcamp.exception.DuplicateCustomerIdException;
import com.nttdata.bootcamp.repository.CustomerRepository;
import com.nttdata.bootcamp.service.CustomerService;
import com.nttdata.bootcamp.service.KafkaService;
import com.nttdata.bootcamp.service.RedisCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

//Service implementation
@Service
public class CustomerServiceImpl implements CustomerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerServiceImpl.class);
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private KafkaService kafkaService;
    @Autowired
    private RedisCacheService redisCacheService;

    @Override
    public Mono<Customer> save(Customer dataCustomer) {
        return Mono.defer(() ->
                customerRepository.existsByDni(dataCustomer.getDni())
                        .doOnSubscribe(s -> LOGGER.info("SUBSCRIBE save() DNI={}", dataCustomer.getDni()))
                        .flatMap(exists -> {
                            if (exists) {
                                LOGGER.info("Customer ya existe. DNI={}", dataCustomer.getDni());
                                return Mono.error(new DuplicateCustomerIdException(dataCustomer.getDni()));
                            }
                            return customerRepository.save(dataCustomer)
                                    .doOnSuccess(saved ->
                                            LOGGER.info("Customer guardado. DNI={} id={}", saved.getDni(), saved.getId())
                                    );
                        })
                        .doOnTerminate(() -> LOGGER.info("TERMINATE save() DNI={}", dataCustomer.getDni()))
                        .doOnError(err -> LOGGER.error("ERROR save() DNI={} -> {}", dataCustomer.getDni(), err.toString()))
                        .log("reactor.pipeline")
        );
    }

    @Override
    public Flux<Customer> findAll() {
        LOGGER.info("Consultando todos lo clientes del banco NTTBANK");
        Flux<Customer> customers = customerRepository.findAll();
        return customers;
    }

    @Override
    public Mono<Customer> findByDni(String dni) {
        // En esta parte use cache por demanda (lazy cache)
        LOGGER.info("Consultando un clientes del banco NTTBANK");
        return redisCacheService.retrieveCustomer(dni)   // 1) BUSCA PRIMERO EN REDIS (rápido)
                .switchIfEmpty(
                        customerRepository.findByDni(dni) // 2) SI NO ESTÁ, BUSCA EN MONGO
                                .flatMap(customer ->
                                        redisCacheService.storeCustomer(dni, customer) // 3) GUARDA EN REDIS
                                                .thenReturn(customer)                  // Y DEVUELVE EL CUSTOMER
                                )
                )
                .switchIfEmpty(Mono.error(new RuntimeException(
                        "Cliente no encontrado con DNI: " + dni
                )));
    }

    @Override
    public Mono<Customer> updateCustomerAddress(Customer dataCustomer) {

        String dni = dataCustomer.getDni();
        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
                    try {

                        Object result = redisCacheService.retrieveCustomer(dni);

                        // 🟩 Caso 1: Redis devolvió un Mono (ej. Mono.just, Mono.empty, MonoOnErrorResume)
                        if (result instanceof Mono) {
                            Mono<?> mono = (Mono<?>) result;

                            return mono
                                    .cast(Customer.class)
                                    .onErrorResume(e -> {
                                        LOGGER.warn("Error obteniendo desde Redis para {}: {}", dni, e.getMessage());
                                        return Mono.empty();
                                    });
                        }

                        // Caso 2: Redis devolvió un objeto normal (Customer)
                        if (result != null) {
                            return Mono.just((Customer) result);
                        }

                        // Caso 3: Redis devolvió null
                        return Mono.<Customer>empty();

                    } catch (Exception e) {
                        LOGGER.warn("Error en Redis para {}: {}", dni, e.getMessage());
                        return Mono.<Customer>empty();
                    }
                }))
                // Aplana Mono<Mono<Customer>> → Mono<Customer>
                .flatMap(mono -> mono)
                .switchIfEmpty(
                        // No está en Redis → buscar en Mongo
                        customerRepository.findAll()
                                .filter(c -> dni.equals(c.getDni()))
                                .next()
                                .switchIfEmpty(Mono.error(new RuntimeException(
                                        "Customer con DNI " + dni + " no existe"
                                )))
                                .flatMap(found ->
                                        Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
                                            redisCacheService.storeCustomer(dni, found);
                                            return found;
                                        }))
                                )
                )
                // Actualizar campos
                .map(customer -> {
                    customer.setAddress(dataCustomer.getAddress());
                    customer.setModificationDate(dataCustomer.getModificationDate());
                    return customer;
                })
                .flatMap(customerRepository::save)
                .doOnNext(updated ->
                        CompletableFuture.runAsync(() ->
                                redisCacheService.storeCustomer(updated.getDni(), updated)
                        )
                );
    }

    @Override
    public Mono<Customer> updateStatus(Customer dataCustomer) {

        String dni = dataCustomer.getDni();
        LOGGER.info("Iniciando UPDATE por DNI: {}", dni);

        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
                    try {

                        Object result = redisCacheService.retrieveCustomer(dni);

                        // Caso 1: Redis devolvió un Mono (ej. Mono.just, Mono.empty, MonoOnErrorResume)
                        if (result instanceof Mono) {
                            Mono<?> mono = (Mono<?>) result;

                            return mono
                                    .cast(Customer.class)
                                    .onErrorResume(e -> {
                                        LOGGER.warn("Error obteniendo desde Redis para {}: {}", dni, e.getMessage());
                                        return Mono.empty();
                                    });
                        }

                        // Caso 2: Redis devolvió un objeto normal (Customer)
                        if (result != null) {
                            return Mono.just((Customer) result);
                        }

                        // Caso 3: Redis devolvió null
                        return Mono.<Customer>empty();

                    } catch (Exception e) {
                        LOGGER.warn("Error en Redis para {}: {}", dni, e.getMessage());
                        return Mono.<Customer>empty();
                    }
                }))
                // Aplana Mono<Mono<Customer>> → Mono<Customer>
                .flatMap(mono -> mono)
                .switchIfEmpty(
                        // No está en Redis → buscar en Mongo
                        customerRepository.findAll()
                                .filter(c -> dni.equals(c.getDni()))
                                .next()
                                .switchIfEmpty(Mono.error(new RuntimeException(
                                        "Customer con DNI " + dni + " no existe"
                                )))
                                .flatMap(found ->
                                        Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
                                            redisCacheService.storeCustomer(dni, found);
                                            return found;
                                        }))
                                )
                )
                // Actualizar campos
                .map(customer -> {
                    customer.setStatus(dataCustomer.getStatus());
                    customer.setModificationDate(dataCustomer.getModificationDate());
                    return customer;
                })
                .flatMap(customerRepository::save)
                .doOnNext(updated ->
                        CompletableFuture.runAsync(() ->
                                redisCacheService.storeCustomer(updated.getDni(), updated)
                        )
                );
    }

    @Override
    public Customer saveInitServices(Customer dataCustomer){
        redisCacheService.storeCustomer(dataCustomer.getDni(), dataCustomer);
        kafkaService.publish(dataCustomer);
        return dataCustomer;
    }

    @Override
    public Mono<Void> delete(String dni) {

        return findByDni(dni)
                .switchIfEmpty(Mono.error(
                        new Error("The customer with DNI " + dni + " does not exist")
                ))
                .flatMap(customerFound ->
                        customerRepository.delete(customerFound)
                )
                .then(
                        redisCacheService.deleteCustomer(dni)
                                .onErrorResume(e -> {
                                    LOGGER.warn("Failed to delete customer {} from Redis: {}", dni, e.getMessage());
                                    return Mono.just(false);
                                })
                )
                .then();
    }


}
