package com.nttdata.bootcamp.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonalCustomerDto {
    @NotBlank
    @Pattern(regexp = "^[0-9]+$", message = "El DNI solo debe contener números")
    @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe tener exactamente 8 dígitos numéricos")
    private String dni;
    private String name;
    private String surName;
    private String address;
    private String phoneNumber;
}
