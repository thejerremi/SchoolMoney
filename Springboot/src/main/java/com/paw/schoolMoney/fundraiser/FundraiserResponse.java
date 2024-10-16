package com.paw.schoolMoney.fundraiser;

import com.paw.schoolMoney.user.UserResponse;
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
public class FundraiserResponse {

    private int id;
    private FundraiserStatus status;
    private BigDecimal goal;
    private BigDecimal currentAmount;
    private BigDecimal availableFunds;
    private String logoPath;
    private String shortDescription;
    private LocalDate startDate;
    private LocalDate endDate;
    private int classId;
    private UserResponse author;
}
