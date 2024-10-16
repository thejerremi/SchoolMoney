package com.paw.schoolMoney.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferForFundraiser {
    private BigDecimal amount;
    private int fundraiserId;
    private int childrenId;
    private int classId;
}
