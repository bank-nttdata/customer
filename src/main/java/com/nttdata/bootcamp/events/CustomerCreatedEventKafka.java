package com.nttdata.bootcamp.events;

import com.nttdata.bootcamp.entity.Customer;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CustomerCreatedEventKafka extends EventKafka<Customer> {

}
