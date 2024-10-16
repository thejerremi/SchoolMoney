package com.paw.schoolMoney.child;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChildRepository extends JpaRepository<Child, Integer>{
    List<Child> findChildsByUserId(int id);
    Child findChildById(int id);
    void deleteChildByIdAndUserId(int id, int userId);
    List<Child> findChildsBy_classId(int classId);
}
