package com.paw.schoolMoney.user;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer> {

  Optional<User> findByEmail(String email);
  Optional<User> findByAccountNumber(String accountNumber);
  Optional<User> findById(int id);
  @Query("SELECT u FROM User u WHERE " +
          "LOWER(u.firstname) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
          "LOWER(u.lastname) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
          "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
  List<User> findParentsBySearchTerm(@Param("search") String search);
  @Query("SELECT u FROM User u JOIN u.classes c WHERE " +
          "(LOWER(u.firstname) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
          "LOWER(u.lastname) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
          "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
          "AND c.id = :classId")
  List<User> findParentsBySearchTermAndClass(@Param("search") String search, @Param("classId") int classId);

}
