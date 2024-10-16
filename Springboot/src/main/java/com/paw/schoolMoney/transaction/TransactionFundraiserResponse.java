package com.paw.schoolMoney.transaction;

import com.paw.schoolMoney.child.ChildResponse;
import com.paw.schoolMoney.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionFundraiserResponse {
    private TransactionType type;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private String description;
    private String child;
    private int childId;
    private int fundraiserId;
    private String parent;
    private int parentId;
}
