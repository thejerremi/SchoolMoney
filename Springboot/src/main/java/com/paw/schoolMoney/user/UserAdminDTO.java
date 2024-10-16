package com.paw.schoolMoney.user;

import com.paw.schoolMoney._class._ClassResponse;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAdminDTO {
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
    private Boolean isNonLocked;
    private List<_ClassResponse> treasurerOfClasses;
}
