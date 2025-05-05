package com.backend.stageconnect.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private MessageType type;
    private Long senderId;
    private Long receiverId;
    private String content;
    private String senderName;
    private String senderPhoto;
    private String conversationId;
    private LocalDateTime timestamp = LocalDateTime.now();
    
    public enum MessageType {
        CHAT,      // Regular chat message
        JOIN,      // User joined the platform
        LEAVE,     // User left/logged out
        TYPING,    // User is typing
        READ       // Message read receipt
    }
} 