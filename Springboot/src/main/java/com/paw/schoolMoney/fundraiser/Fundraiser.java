package com.paw.schoolMoney.fundraiser;

import com.paw.schoolMoney.transaction.Transaction;
import com.paw.schoolMoney.user.User;
import com.paw.schoolMoney._class._Class;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;  // Import BigDecimal dla celów finansowych
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fundraisers")
public class Fundraiser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    private FundraiserStatus status;

    private BigDecimal goal;
    private BigDecimal currentAmount = BigDecimal.ZERO;
    private BigDecimal availableFunds = BigDecimal.ZERO;

    private String logoPath;
    private String shortDescription;
    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private _Class _class;

    @OneToMany(mappedBy = "fundraiser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions;

    // Metoda do aktualizacji zebranej kwoty
    public void addToCurrentAmount(BigDecimal amount) {
        this.currentAmount = this.currentAmount.add(amount);
    }
    public void addToAvailableFunds(BigDecimal amount) {
        this.availableFunds = this.availableFunds.add(amount);
    }
}
