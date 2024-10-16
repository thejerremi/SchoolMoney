package com.paw.schoolMoney._class;

import com.paw.schoolMoney.child.Child;
import com.paw.schoolMoney.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface _ClassRepository extends JpaRepository<_Class, Integer> {
    @Query("SELECT c FROM _Class c JOIN c.parents p WHERE p.id = :parentId")
    List<_Class> findByParentId(@Param("parentId") int parentId);
    @Query("SELECT c FROM _Class c WHERE LOWER(c.className) = LOWER(:className)")
    Optional<_Class> findByClassName(@Param("className") String className);
    @Query("SELECT ch.id FROM Child ch WHERE ch._class.id = :classId")
    List<Integer> findChildrenByClassId(@Param("classId") int classId);
    Optional<_Class> findById(int id);
    List<_Class> findAllByTreasurerId(int id);
}