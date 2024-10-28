package com.paw.schoolMoney.child;

import com.paw.schoolMoney._class._Class;
import com.paw.schoolMoney.transaction.Transaction;
import com.paw.schoolMoney.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "children")
public class Child {

    @Id
    @GeneratedValue
    private Integer id;
    private String firstname;
    private String lastname;
    private LocalDate dob;
    private String accountNumber;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private _Class _class;
    @OneToMany(mappedBy = "children")
    private List<Transaction> transactions;
    @PreRemove
    private void preRemove() {
        for (Transaction transaction : transactions) {
            transaction.setChildren(null);
        }
    }

}
