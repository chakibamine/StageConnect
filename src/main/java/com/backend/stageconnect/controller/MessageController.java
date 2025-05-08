package com.backend.stageconnect.controller;

import com.backend.stageconnect.dto.ConversationDTO;
import com.backend.stageconnect.dto.MessageDTO;
import com.backend.stageconnect.entity.Message;
import com.backend.stageconnect.entity.User;
import com.backend.stageconnect.repository.ConnectionRepository;
import com.backend.stageconnect.repository.MessageRepository;
import com.backend.stageconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(
    origins = {"http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173", "http://127.0.0.1:3000"},
    allowCredentials = "true",
    allowedHeaders = {"Origin", "Content-Type", "Accept", "Authorization", "X-Requested-With", "Access-Control-Request-Method", "Access-Control-Request-Headers", "x-cors-debug"},
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ConnectionRepository connectionRepository;
    
    // Get all conversations for a user
    @GetMapping("/conversations/{userId}")
    public ResponseEntity<?> getUserConversations(@PathVariable Long userId) {
        try {
            // Check if the user exists
            if (!userRepository.existsById(userId)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found with ID: " + userId
                ));
            }
            
            // Get all users that the current user has messaged with
            List<Long> partnerIds = messageRepository.findUserConversationPartnerIds(userId);
            
            // For each partner, get the latest message and unread count
            List<ConversationDTO> conversations = new ArrayList<>();
            
            for (Long partnerId : partnerIds) {
                // Get partner user
                User partner = userRepository.findById(partnerId).orElse(null);
                if (partner == null) continue;
                
                // Get the latest message
                Pageable topOne = PageRequest.of(0, 1, Sort.by("createdAt").descending());
                List<Message> latestMessages = messageRepository.findLatestMessageBetweenUsers(userId, partnerId, topOne);
                Message latestMessage = latestMessages.isEmpty() ? null : latestMessages.get(0);
                
                // Count unread messages from this partner
                long unreadCount = messageRepository.countByReceiverIdAndSenderIdAndIsReadFalse(userId, partnerId);
                
                // Create conversation DTO (without full message list)
                ConversationDTO conversation = ConversationDTO.fromPartnerAndLastMessage(
                    partner, latestMessage, (int) unreadCount, userId, null);
                
                conversations.add(conversation);
            }
            
            // Sort by latest message timestamp
            conversations.sort((c1, c2) -> {
                if (c1.getLastMessage() == null) return 1;
                if (c2.getLastMessage() == null) return -1;
                return c2.getLastMessage().getTimestamp().compareTo(c1.getLastMessage().getTimestamp());
            });
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "conversations", conversations
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to fetch conversations: " + e.getMessage()
            ));
        }
    }
    
    // Get messages between two users
    @GetMapping("/{userId}/{partnerId}")
    public ResponseEntity<?> getConversation(
            @PathVariable Long userId,
            @PathVariable Long partnerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        try {
            // Check if both users exist
            User user = userRepository.findById(userId).orElse(null);
            User partner = userRepository.findById(partnerId).orElse(null);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found with ID: " + userId
                ));
            }
            
            if (partner == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Partner not found with ID: " + partnerId
                ));
            }
            
            // Optional: Check if users are connected (if you want to restrict messaging to connections)
            // Comment this out if you want to allow messaging between any users
            /*
            if (!connectionRepository.findExistingConnection(userId, partnerId).isPresent()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "You must be connected with this user to message them"
                ));
            }
            */
            
            // Get all messages between users (most recent first for pagination, then reversed for display)
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            List<Message> messages = messageRepository.findMessagesBetweenUsers(userId, partnerId, pageable).getContent();
            
            // Reverse to get chronological order
            Collections.reverse(messages);
            
            // Count unread messages from this partner
            long unreadCount = messageRepository.countByReceiverIdAndSenderIdAndIsReadFalse(userId, partnerId);
            
            // Get the latest message for the conversation summary
            Message latestMessage = messages.isEmpty() ? null : messages.get(messages.size() - 1);
            
            // Create conversation DTO with full message list
            ConversationDTO conversation = ConversationDTO.fromPartnerAndLastMessage(
                partner, latestMessage, (int) unreadCount, userId, messages);
            
            // Mark messages as read
            if (!messages.isEmpty()) {
                List<Message> unreadMessages = messages.stream()
                    .filter(m -> m.getReceiver().getId().equals(userId) && !m.isRead())
                    .collect(Collectors.toList());
                
                if (!unreadMessages.isEmpty()) {
                    unreadMessages.forEach(m -> m.setRead(true));
                    messageRepository.saveAll(unreadMessages);
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "conversation", conversation
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to fetch conversation: " + e.getMessage()
            ));
        }
    }
    
    // Send a message
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> requestBody) {
        try {
            // Extract details from request
            Long senderId = Long.valueOf(requestBody.get("sender_id").toString());
            Long receiverId = Long.valueOf(requestBody.get("receiver_id").toString());
            String content = (String) requestBody.get("content");
            
            // Validate input
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Message content cannot be empty"
                ));
            }
            
            // Check if both users exist
            User sender = userRepository.findById(senderId).orElse(null);
            User receiver = userRepository.findById(receiverId).orElse(null);
            
            if (sender == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Sender not found with ID: " + senderId
                ));
            }
            
            if (receiver == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Receiver not found with ID: " + receiverId
                ));
            }
            
            // Optional: Check if users are connected (if you want to restrict messaging to connections)
            // Comment this out if you want to allow messaging between any users
            /*
            if (!connectionRepository.findExistingConnection(senderId, receiverId).isPresent()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "You must be connected with this user to message them"
                ));
            }
            */
            
            // Create and save the message
            Message message = new Message();
            message.setSender(sender);
            message.setReceiver(receiver);
            message.setContent(content);
            message.setRead(false);
            
            // Set conversation ID (if you're using it)
            message.setConversationId(Message.generateConversationId(senderId, receiverId));
            
            Message savedMessage = messageRepository.save(message);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Message sent successfully",
                "data", MessageDTO.fromEntity(savedMessage)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to send message: " + e.getMessage()
            ));
        }
    }
    
    // Mark messages as read
    @PutMapping("/read/{userId}/{partnerId}")
    public ResponseEntity<?> markMessagesAsRead(
            @PathVariable Long userId,
            @PathVariable Long partnerId) {
        
        try {
            // Find unread messages from partner to user
            List<Message> unreadMessages = messageRepository.findMessagesBetweenUsers(userId, partnerId).stream()
                .filter(m -> m.getReceiver().getId().equals(userId) && !m.isRead())
                .collect(Collectors.toList());
            
            if (!unreadMessages.isEmpty()) {
                unreadMessages.forEach(m -> m.setRead(true));
                messageRepository.saveAll(unreadMessages);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Messages marked as read",
                "count", unreadMessages.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to mark messages as read: " + e.getMessage()
            ));
        }
    }
    
    // Get user's unread message count
    @GetMapping("/unread/{userId}")
    public ResponseEntity<?> getUnreadMessageCount(@PathVariable Long userId) {
        try {
            long count = messageRepository.countByReceiverIdAndIsReadFalse(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "unreadCount", count
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to get unread message count: " + e.getMessage()
            ));
        }
    }
    
    // Get all messages by conversation ID
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<?> getMessagesByConversationId(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        try {
            // Validate the conversation ID format (should be userId1_userId2)
            if (!conversationId.contains("_")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid conversation ID format. Expected: userId1_userId2"
                ));
            }
            
            // Get messages by conversation ID with pagination
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
            
            // Convert messages to DTOs
            List<MessageDTO> messageDTOs = messages.stream()
                .map(MessageDTO::fromEntity)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "messages", messageDTOs,
                "count", messageDTOs.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to fetch messages: " + e.getMessage()
            ));
        }
    }
} 