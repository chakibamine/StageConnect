package com.backend.stageconnect.controller;

import com.backend.stageconnect.dto.MessageDTO;
import com.backend.stageconnect.entity.Message;
import com.backend.stageconnect.entity.User;
import com.backend.stageconnect.model.ChatMessage;
import com.backend.stageconnect.repository.MessageRepository;
import com.backend.stageconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Handle chat messages sent from the client
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        try {
            // Generate conversation ID if not provided
            if (chatMessage.getConversationId() == null) {
                chatMessage.setConversationId(Message.generateConversationId(
                    chatMessage.getSenderId(), chatMessage.getReceiverId()));
            }
            
            // Set timestamp if not provided
            if (chatMessage.getTimestamp() == null) {
                chatMessage.setTimestamp(java.time.LocalDateTime.now());
            }
            
            // Save the message to database if it's a CHAT type
            if (chatMessage.getType() == ChatMessage.MessageType.CHAT) {
                // Find sender and receiver
                User sender = userRepository.findById(chatMessage.getSenderId()).orElse(null);
                User receiver = userRepository.findById(chatMessage.getReceiverId()).orElse(null);
                
                if (sender != null && receiver != null) {
                    // Create and save the message entity
                    Message message = new Message();
                    message.setSender(sender);
                    message.setReceiver(receiver);
                    message.setContent(chatMessage.getContent());
                    message.setRead(false);
                    message.setConversationId(chatMessage.getConversationId());
                    
                    Message savedMessage = messageRepository.save(message);
                    
                    // Update chat message with the database ID
                    chatMessage.setContent(savedMessage.getContent());
                }
            }
            
            // Send the message to the specific user's topic
            // This will send to both users in the conversation
            messagingTemplate.convertAndSend(
                "/topic/user/" + chatMessage.getReceiverId(),
                chatMessage
            );
            
            // Also send to sender for confirmation (optional, depending on how your client works)
            messagingTemplate.convertAndSend(
                "/topic/user/" + chatMessage.getSenderId(),
                chatMessage
            );
            
        } catch (Exception e) {
            e.printStackTrace();
            // You could send an error message back if needed
        }
    }
    
    // Handle user join events (when a user connects to the WebSocket)
    @MessageMapping("/chat.join")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // Add user to the WebSocket session
        headerAccessor.getSessionAttributes().put("USER_ID", chatMessage.getSenderId());
        
        // You may want to broadcast to connected users (friends/connections) that this user is online
        messagingTemplate.convertAndSend("/topic/public", chatMessage);
    }
    
    // Handle typing indicators
    @MessageMapping("/chat.typing")
    public void typing(@Payload ChatMessage chatMessage) {
        // Forward typing status to the receiver
        messagingTemplate.convertAndSend(
            "/topic/user/" + chatMessage.getReceiverId(),
            chatMessage
        );
    }
    
    // Handle read receipts
    @MessageMapping("/chat.read")
    public void markAsRead(@Payload ChatMessage chatMessage) {
        try {
            // Update read status in database
            if (chatMessage.getConversationId() != null) {
                // Find unread messages from sender to receiver in this conversation
                String[] userIds = chatMessage.getConversationId().split("_");
                if (userIds.length == 2) {
                    Long userId1 = Long.parseLong(userIds[0]);
                    Long userId2 = Long.parseLong(userIds[1]);
                    
                    // Get messages between the two users
                    messageRepository.findMessagesBetweenUsers(userId1, userId2).stream()
                        .filter(m -> m.getSender().getId().equals(chatMessage.getSenderId()) && 
                               m.getReceiver().getId().equals(chatMessage.getReceiverId()) &&
                               !m.isRead())
                        .forEach(m -> {
                            m.setRead(true);
                            messageRepository.save(m);
                        });
                }
            }
            
            // Notify sender that messages have been read
            messagingTemplate.convertAndSend(
                "/topic/user/" + chatMessage.getSenderId(),
                chatMessage
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 