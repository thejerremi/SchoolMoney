package com.paw.schoolMoney.fundraiser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FundraiserRepository extends JpaRepository<Fundraiser, Integer> {

    List<Fundraiser> findBy_class_Id(int classId);

    List<Fundraiser> findByAuthor_Id(int authorId);
    List<Fundraiser> findAllByStartDateAndStatus(LocalDate startDate, FundraiserStatus status);
    List<Fundraiser> findAllByEndDateBeforeAndStatus(LocalDate startDate, FundraiserStatus status);
}
