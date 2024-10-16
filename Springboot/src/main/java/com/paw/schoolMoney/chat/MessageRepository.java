package com.paw.schoolMoney.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findByClassId(Integer classId); // For group chat history
    @Query("SELECT m FROM Message m WHERE (m.senderId = :senderId AND m.recipientId = :recipientId) OR (m.senderId = :recipientId AND m.recipientId = :senderId)")
    List<Message> findMessagesBetweenUsers(Integer senderId, Integer recipientId);

}
