package com.backend.stageconnect.dto;

import com.backend.stageconnect.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long id;
    private Long senderId;
    private String senderName;
    private String senderPhoto;
    private Long receiverId;
    private String receiverName;
    private String receiverPhoto;
    private String content;
    private boolean isRead;
    private LocalDateTime timestamp;
    
    public static MessageDTO fromEntity(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        
        // Set sender info
        if (message.getSender() != null) {
            dto.setSenderId(message.getSender().getId());
            dto.setSenderName(message.getSender().getFirstName() + " " + message.getSender().getLastName());
            
            // Set photo if available (you may need to adapt this based on your User entity)
            try {
                String className = message.getSender().getClass().getSimpleName();
                if (className.equals("Candidate")) {
                    dto.setSenderPhoto((String) getPropertyValue(message.getSender(), "photo"));
                }
            } catch (Exception e) {
                // Ignore if photo is not available
            }
        }
        
        // Set receiver info
        if (message.getReceiver() != null) {
            dto.setReceiverId(message.getReceiver().getId());
            dto.setReceiverName(message.getReceiver().getFirstName() + " " + message.getReceiver().getLastName());
            
            // Set photo if available
            try {
                String className = message.getReceiver().getClass().getSimpleName();
                if (className.equals("Candidate")) {
                    dto.setReceiverPhoto((String) getPropertyValue(message.getReceiver(), "photo"));
                }
            } catch (Exception e) {
                // Ignore if photo is not available
            }
        }
        
        dto.setContent(message.getContent());
        dto.setRead(message.isRead());
        dto.setTimestamp(message.getCreatedAt());
        
        return dto;
    }
    
    // Helper method to safely get a property value using reflection
    private static Object getPropertyValue(Object object, String propertyName) {
        try {
            // Try to find a getter method first (e.g., getName for "name" property)
            String getterName = "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
            java.lang.reflect.Method getter = object.getClass().getMethod(getterName);
            return getter.invoke(object);
        } catch (Exception e) {
            return null;
        }
    }
} 