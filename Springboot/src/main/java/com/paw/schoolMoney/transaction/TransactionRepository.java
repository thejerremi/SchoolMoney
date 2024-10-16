package com.paw.schoolMoney.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    List<Transaction> findTop5ByUserIdOrderByCreatedAtDesc(Integer userId);

    Page<Transaction> findByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

    List<Transaction> findAllByFundraiserId(Integer fundraiserId);

    @Query("SELECT t FROM Transaction t WHERE t.fundraiser.id = :fundraiserId")
    List<Transaction> findByFundraiserId(@Param("fundraiserId") int fundraiserId);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :parentId AND t.fundraiser.id = :fundraiserId")
    List<Transaction> findByFundraiserIdAndParentId(@Param("fundraiserId") int fundraiserId, @Param("parentId") int parentId);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :parentId")
    List<Transaction> findByParentId(@Param("parentId") int parentId);

    @Query("SELECT t FROM Transaction t WHERE t.fundraiser._class.id = :classId")
    List<Transaction> findByClassId(@Param("classId") int classId);

    @Query("SELECT t FROM Transaction t WHERE t.fundraiser._class.id = :classId AND t.user.id = :parentId")
    List<Transaction> findByClassIdAndParentId(@Param("classId") int classId, @Param("parentId") int parentId);
}
