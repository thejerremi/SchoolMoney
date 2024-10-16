package com.paw.schoolMoney.chat;

import com.paw.schoolMoney._class._Class;
import com.paw.schoolMoney._class._ClassRepository;
import com.paw.schoolMoney.auth.AuthenticationService;
import com.paw.schoolMoney.user.Role;
import com.paw.schoolMoney.user.User;
import com.paw.schoolMoney.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class MessageController {
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final AuthenticationService authService;
    private final UserRepository userRepository;
    private final _ClassRepository classRepository;


    @MessageMapping("/message")
    public void sendGroupMessage(@Payload Message chatMessage) {
        if (chatMessage.getClassId() == null) {
            throw new IllegalArgumentException("ClassId cannot be null.");
        }

        Integer senderId = chatMessage.getSenderId();
        User user = userRepository.findById(senderId).orElse(null);
        // Save the message to the database
        Message message = new Message();
        message.setContent(chatMessage.getContent());
        message.setSenderId(senderId);
        if(user != null) {
            message.setSenderName(user.getFirstname() + " " + user.getLastname());
        }
        message.setClassId(chatMessage.getClassId());
        message.setTimestamp(LocalDateTime.now());

        messageService.saveMessage(message);

        // Send the message to the appropriate topic
        messagingTemplate.convertAndSend("/topic/group/" + chatMessage.getClassId(), chatMessage);
    }

    @MessageMapping("/private-message")
    public void sendPrivateMessage(@Payload Message chatMessage, final Principal principal) throws InterruptedException {
        User sender = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));
        User recipient = userRepository.findByEmail(chatMessage.getRecipientUsername())
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));

        // Create and save the message to the database
        Message message = new Message();
        message.setContent(chatMessage.getContent());
        message.setTimestamp(LocalDateTime.now());

        message.setSenderUsername(sender.getEmail());
        message.setSenderId(sender.getId());
        message.setSenderName(sender.getFirstname() + " " + sender.getLastname());

        message.setRecipientUsername(recipient.getEmail());
        message.setRecipientId(recipient.getId());
        message.setRecipientName(recipient.getFirstname() + " " + recipient.getLastname());


        // Zapisz wiadomość w bazie danych
        messageService.saveMessage(message);
        messagingTemplate.convertAndSendToUser(chatMessage.getRecipientUsername(), "/topic/private-messages", message);
    }

    // Get group chat history with access control
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/group/{classId}")
    public ResponseEntity<?> getClassChatHistory(@PathVariable Integer classId, @RequestHeader("Authorization") String token) {
        // Find user by token
        Optional<User> user = authService.findUserByToken(token);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found or access token is invalid.");
        }

        // Check if user has access to the class
        if (!userHasAccessToClass(user.get(), classId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have access to this class.");
        }

        List<Message> messages = messageService.getClassChatHistory(classId);
        return ResponseEntity.ok(messages);
    }

    // Get private chat history with access control
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/private/{senderId}/{recipientId}")
    public ResponseEntity<?> getPrivateChatHistory(@PathVariable Integer senderId, @PathVariable Integer recipientId, @RequestHeader("Authorization") String token) {
        // Find user by token
        Optional<User> user = authService.findUserByToken(token);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found or access token is invalid.");
        }

        // Check if the user is either the sender or the recipient of the messages
        if (!userIsSenderOrRecipient(user.get(), senderId, recipientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have access to this private chat.");
        }

        List<Message> messages = messageService.getPrivateChatHistory(senderId, recipientId);
        return ResponseEntity.ok(messages);
    }

    // Helper methods for access control
    private boolean userHasAccessToClass(User user, Integer classId) {
        if(user.getRole() == Role.ADMIN)
            return true;
        List<_Class> classes = classRepository.findByParentId(user.getId());
        return classes.stream().anyMatch(c -> Objects.equals(c.getId(), classId));
    }

    private boolean userIsSenderOrRecipient(User user, Integer senderId, Integer recipientId) {
        return user.getId().equals(senderId) || user.getId().equals(recipientId);
    }
}
