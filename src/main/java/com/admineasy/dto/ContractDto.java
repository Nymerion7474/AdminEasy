// src/main/java/com/admineasy/dto/ContractDto.java
package com.admineasy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractDto {

    private Long id;

    @NotNull
    private String contractNumber;

    @NotNull
    private String name;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private BigDecimal amount;

    private String currency;

    private String contractType;

    private String paymentFrequency;

    private boolean autoRenew;

    private String providerContact;

    private String notes;

    // En sortie uniquement :
    private String status;
}
