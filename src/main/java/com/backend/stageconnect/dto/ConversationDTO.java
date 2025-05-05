package com.backend.stageconnect.dto;

import com.backend.stageconnect.entity.Message;
import com.backend.stageconnect.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    // The unique ID of the conversation (could be generated from user IDs)
    private String id;
    
    // The other user in the conversation
    private UserSummaryDTO user;
    
    // Information about the last message
    private LastMessageDTO lastMessage;
    
    // Count of unread messages
    private int unreadCount;
    
    // Optional: full list of messages
    private List<MessageDTO> messages;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummaryDTO {
        private Long id;
        private String name;
        private String profilePicture;
        private boolean isOnline; // This would need a separate online tracking mechanism
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LastMessageDTO {
        private String content;
        private LocalDateTime timestamp;
        private boolean isRead;
    }
    
    // Create a ConversationDTO from a partner user and the latest message
    public static ConversationDTO fromPartnerAndLastMessage(
            User partner, 
            Message lastMessage, 
            int unreadCount, 
            Long currentUserId,
            List<Message> allMessages) {
        
        ConversationDTO dto = new ConversationDTO();
        
        // Generate conversation ID
        dto.setId(Message.generateConversationId(currentUserId, partner.getId()));
        
        // Set partner user info
        UserSummaryDTO userDto = new UserSummaryDTO();
        userDto.setId(partner.getId());
        userDto.setName(partner.getFirstName() + " " + partner.getLastName());
        
        // Set profile picture if available
        try {
            String className = partner.getClass().getSimpleName();
            if (className.equals("Candidate")) {
                userDto.setProfilePicture((String) getPropertyValue(partner, "photo"));
            }
        } catch (Exception e) {
            // Default or empty if not available
        }
        
        // For online status, you would need a separate mechanism
        // This is a placeholder - implement real online status tracking in a production app
        userDto.setOnline(false);
        
        dto.setUser(userDto);
        
        // Set last message info
        if (lastMessage != null) {
            LastMessageDTO lastMessageDto = new LastMessageDTO();
            lastMessageDto.setContent(lastMessage.getContent());
            lastMessageDto.setTimestamp(lastMessage.getCreatedAt());
            lastMessageDto.setRead(lastMessage.isRead() || lastMessage.getSender().getId().equals(currentUserId));
            dto.setLastMessage(lastMessageDto);
        }
        
        dto.setUnreadCount(unreadCount);
        
        // Set all messages if provided
        if (allMessages != null) {
            dto.setMessages(allMessages.stream()
                    .map(MessageDTO::fromEntity)
                    .collect(Collectors.toList()));
        }
        
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