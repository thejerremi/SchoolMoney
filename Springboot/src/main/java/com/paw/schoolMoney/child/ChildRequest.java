package com.paw.schoolMoney.child;

import com.paw.schoolMoney._class._Class;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChildRequest {
    private int id;
    private String firstName;
    private String lastName;
    private LocalDate dob;
    private Integer _class;
}
