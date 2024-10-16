package com.paw.schoolMoney.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private int id;
    private String firstname;
    private String lastname;
    private String email;
    private String pesel;
    private LocalDate dob;
    private BigDecimal balance;
    private String accountNumber;
    private Role role;
    private String username;
}
