package com.nttdata.bootcamp.entity.dto;


import com.nttdata.bootcamp.entity.Customer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreateResponse {
    private String message;
    private Customer customer;

    public static CustomerCreateResponse success(Customer c) {
        return CustomerCreateResponse.builder()
                .message("El DNI fue registrado correctamente")
                .customer(c)
                .build();
    }
}
