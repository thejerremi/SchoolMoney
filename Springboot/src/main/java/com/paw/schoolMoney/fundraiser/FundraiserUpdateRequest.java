package com.paw.schoolMoney.fundraiser;

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
public class FundraiserUpdateRequest {
    private BigDecimal goal;
    private String shortDescription;
    private LocalDate startDate;
    private LocalDate endDate;
}
