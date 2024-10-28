package com.paw.schoolMoney._class;

import com.paw.schoolMoney.child.Child;
import com.paw.schoolMoney.user.User;
import jakarta.persistence.*;
        import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_class")
public class _Class {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String className;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private List<Child> children;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treasurer_id", referencedColumnName = "id")
    private User treasurer;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinTable(
            name = "class_parents",
            joinColumns = @JoinColumn(name = "class_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> parents;

}

