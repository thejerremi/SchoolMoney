package com.paw.schoolMoney.chat;

import com.paw.schoolMoney.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String content;
    private LocalDateTime timestamp;

    private Integer classId; // null for private chat

    private String senderUsername;
    private Integer senderId;
    private String senderName;

    private String recipientUsername; // null for group chat
    private Integer recipientId; // null for group chat
    private String recipientName; // null for group chat

}
