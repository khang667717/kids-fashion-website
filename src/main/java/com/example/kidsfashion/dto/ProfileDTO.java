package com.example.kidsfashion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO for updating user profile information.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {
    private String fullName;
    private String email;
    private String phone;
    private String gender;    // MALE, FEMALE, OTHER
    private LocalDate birthday;
}
