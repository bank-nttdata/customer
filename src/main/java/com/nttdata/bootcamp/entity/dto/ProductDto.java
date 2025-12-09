package com.nttdata.bootcamp.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {
    @NotBlank
    @Pattern(regexp = "^[0-9]+$", message = "El DNI solo debe contener números")
    private String dni;
    private String typeCustomer;
    private String accountNumber;
    private String accountType;
    private double balance;

}
