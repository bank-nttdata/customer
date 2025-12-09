package com.nttdata.bootcamp.service;

import com.nttdata.bootcamp.entity.Customer;

public interface KafkaService {
    void publish(Customer customer);
}
