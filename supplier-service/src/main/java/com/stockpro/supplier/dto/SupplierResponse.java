package com.stockpro.supplier.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupplierResponse {
    private Long supplierId;
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String country;
    private String taxId;
    private String paymentTerms;
    private Integer leadTimeDays;
    private Double rating;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
