package com.paw.schoolMoney.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    public List<Message> getClassChatHistory(Integer classId) {
        return messageRepository.findByClassId(classId);
    }

    public List<Message> getPrivateChatHistory(Integer senderId, Integer recipientId) {
        return messageRepository.findMessagesBetweenUsers(senderId, recipientId);
    }

    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }
}
