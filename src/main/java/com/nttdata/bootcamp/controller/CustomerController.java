package com.nttdata.bootcamp.controller;

import com.nttdata.bootcamp.entity.dto.BusinessCustomerDto;
import com.nttdata.bootcamp.entity.dto.UpdateAddressDto;
import com.nttdata.bootcamp.entity.dto.PersonalCustomerDto;
import com.nttdata.bootcamp.entity.dto.UpdateStatusDto;
import com.nttdata.bootcamp.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nttdata.bootcamp.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.nttdata.bootcamp.entity.Customer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Date;
import javax.validation.Valid;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/customer")
public class CustomerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerService customerService;

    // ===========================
    // CREATE PERSONAL CUSTOMER
    // ===========================
    @PostMapping("/savePersonalCustomer")
    public Mono<Customer> savePersonalCustomer(@Valid @RequestBody PersonalCustomerDto dto) {

        Customer dataCustomer = mapFromPersonal(dto);

        return customerService.save(dataCustomer)
                .flatMap(saved ->
                        Mono.fromRunnable(() -> customerService.saveInitServices(saved))
                                .thenReturn(saved)
                )
                .doOnSubscribe(s -> LOGGER.info("SUBSCRIBE create DNI={}", dataCustomer.getDni()))
                .doOnSuccess(saved -> LOGGER.info("OK create DNI={} id={}", saved.getDni(), saved.getId()))
                .doOnError(e -> LOGGER.error("ERROR create DNI={} -> {}", dataCustomer.getDni(), e.toString()));
    }


    // ===========================
    // CREATE BUSINESS CUSTOMER
    // ===========================
    @PostMapping("/saveBusinessCustomer")
    public Mono<Customer> saveBusinessCustomer(@RequestBody BusinessCustomerDto dto) {

        Customer dataCustomer = mapFromBussines(dto);

        return customerService.save(dataCustomer)
                .flatMap(saved ->
                        Mono.fromRunnable(() -> customerService.saveInitServices(saved))
                                .thenReturn(saved)
                )
                .doOnSubscribe(s -> LOGGER.info("SUBSCRIBE create DNI={}", dataCustomer.getDni()))
                .doOnSuccess(saved -> LOGGER.info("OK create DNI={} id={}", saved.getDni(), saved.getId()))
                .doOnError(e -> LOGGER.error("ERROR create DNI={} -> {}", dataCustomer.getDni(), e.toString()));
    }


    // ===========================
    // FIND ALL CUSTOMERS
    // ===========================
    @GetMapping("/")
    public Flux<Customer> findAllCustomers() {
        return customerService.findAll();
    }


    // ===========================
    // FIND BY DNI
    // ===========================
    @GetMapping("/findByClient/{dni}")
    public Mono<Customer> findByClient(@PathVariable String dni) {
        return customerService.findByDni(dni);
    }


    // ===========================
    // UPDATE CUSTOMER ADDRESS
    // ===========================
    @PutMapping("/updateCustomerAddress/{dni}")
    public Mono<Customer> updateCustomerAddress(
            @PathVariable String dni,
            @Valid @RequestBody UpdateAddressDto dto) {

        Customer c = new Customer();
        c.setDni(dni);
        c.setAddress(dto.getAddress());
        c.setModificationDate(new Date());

        return customerService.updateCustomerAddress(c);
    }


    // ===========================
    // UPDATE CUSTOMER STATUS
    // ===========================
    @PutMapping("/updateCustomerStatus/{dni}")
    public Mono<Customer> updateCustomerStatus(
            @PathVariable String dni,
            @Valid @RequestBody UpdateStatusDto dto) {

        Customer c = new Customer();
        c.setDni(dni);
        c.setStatus(dto.getStatus());
        c.setModificationDate(new Date());

        return customerService.updateStatus(c);
    }


    // ===========================
    // DELETE CUSTOMER
    // ===========================
    @DeleteMapping("/delete/{dni}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteCustomer(@PathVariable String dni) {
        return customerService.delete(dni);
    }


    // MAPPERS
    private Customer mapFromPersonal(PersonalCustomerDto dto) {
        Customer c = new Customer();
        c.setDni(dto.getDni());
        c.setTypeCustomer(Constant.PERSONAL_CUSTOMER);
        c.setFlagVip(false);
        c.setFlagPyme(false);
        c.setName(dto.getName());
        c.setSurName(dto.getSurName());
        c.setPhoneNumber(dto.getPhoneNumber());
        c.setAddress(dto.getAddress());
        c.setStatus(Constant.CUSTOMER_ACTIVE);
        c.setCreationDate(new Date());
        c.setModificationDate(new Date());
        return c;
    }

    private Customer mapFromBussines(BusinessCustomerDto dto) {
        Customer c = new Customer();
        c.setDni(dto.getDni());
        c.setTypeCustomer(Constant.BUSINESS_CUSTOMER);
        c.setFlagVip(false);
        c.setFlagPyme(false);
        c.setName(dto.getName());
        c.setSurName(dto.getSurName());
        c.setPhoneNumber(dto.getPhoneNumber());
        c.setAddress(dto.getAddress());
        c.setStatus(Constant.CUSTOMER_ACTIVE);
        c.setCreationDate(new Date());
        c.setModificationDate(new Date());
        return c;
    }
}