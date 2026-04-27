package com.stockpro.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminUpdateUserRequest {
    private String fullName;
    private String phone;
    private String department;
    private String role;
    private Boolean isActive;
}
