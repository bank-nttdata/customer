package com.nttdata.bootcamp.service.impl;

import com.nttdata.bootcamp.entity.Customer;
import com.nttdata.bootcamp.repository.CustomerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class CustomerServiceImplTest {

    @InjectMocks
    CustomerServiceImpl customerServiceImpl;

    @Mock
    CustomerRepository customerRepository;

    public Customer customer;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void findAll() {
    }

//    @Test
//    void findByDni() {
//        Customer customer = new Customer();
//        customer.setId("123456");
//        customer.setDni("03700780");
//        customer.setTypeCustomer("PERSONAL");
//        customer.setFlagVip(true);
//        customer.setFlagPyme(true);
//        customer.setName("Jorge");
//        customer.setSurName("Odar");
//        customer.setAddress("Jose Leonardo Ortiz");
//        customer.setStatus("ACTIVE");
//        customer.setCreationDate(new Date());
//        customer.setModificationDate(new Date());
//
//        Mono<Customer> customerMono = Mono.just(customer);
//        when(customerRepository.findById("72384351").thenReturn(customerMono));
//        Mono<Customer> customerMonoT = customerServiceImpl.findByDni("72384351");
//        assertNotNull(customerMonoT);
//        assertEquals("72384351", customerMono.block().getDni());
//    }

    @Test
    void save() {
    }
}