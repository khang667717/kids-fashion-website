package com.example.kidsfashion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for Address create/update form.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    private Long id;
    private String fullName;
    private String phone;
    private String addressLine;
    private String ward;
    private String district;
    private String city;
    private String label;      // HOME, WORK, OTHER
    private Boolean isDefault;
}
