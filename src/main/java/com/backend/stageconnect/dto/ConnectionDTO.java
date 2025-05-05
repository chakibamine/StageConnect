package com.backend.stageconnect.dto;

import com.backend.stageconnect.entity.Connection;
import com.backend.stageconnect.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionDTO {
    private Long id;
    private Long requesterId;
    private String requesterName;
    private String requesterTitle;
    private String requesterCompany;
    private String requesterLocation;
    private String requesterPhoto;
    
    private Long receiverId;
    private String receiverName;
    private String receiverTitle;
    private String receiverCompany;
    private String receiverLocation;
    private String receiverPhoto;
    
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // User for the current perspective (the one viewing this connection)
    private boolean isUserRequester;
    
    public static ConnectionDTO fromEntity(Connection connection, Long currentUserId) {
        ConnectionDTO dto = new ConnectionDTO();
        dto.setId(connection.getId());
        
        // Set requester info
        User requester = connection.getRequester();
        if (requester != null) {
            dto.setRequesterId(requester.getId());
            // For a real implementation, adapt these fields according to your User entity
            dto.setRequesterName(requester.getFirstName() + " " + requester.getLastName());
            
            // These fields would need to be adapted based on your actual User entity structure
            // Use instanceof with caution and check class name if direct casting isn't possible
            String className = requester.getClass().getSimpleName();
            if (className.equals("Candidate")) {
                try {
                    // Use reflection or other safe approach if direct casting isn't possible
                    // This is a simplified example - adapt to your actual User hierarchy
                    dto.setRequesterTitle((String) getPropertyValue(requester, "title"));
                    dto.setRequesterLocation((String) getPropertyValue(requester, "location"));
                    dto.setRequesterPhoto((String) getPropertyValue(requester, "photo"));
                } catch (Exception e) {
                    // Log the error but continue with partial data
                    System.err.println("Error getting Candidate properties: " + e.getMessage());
                }
            } else if (className.equals("Company")) {
                try {
                    dto.setRequesterCompany((String) getPropertyValue(requester, "name"));
                } catch (Exception e) {
                    // Log the error but continue with partial data
                    System.err.println("Error getting Company properties: " + e.getMessage());
                }
            }
        }
        
        // Set receiver info
        User receiver = connection.getReceiver();
        if (receiver != null) {
            dto.setReceiverId(receiver.getId());
            dto.setReceiverName(receiver.getFirstName() + " " + receiver.getLastName());
            
            // Similar approach for receiver
            String className = receiver.getClass().getSimpleName();
            if (className.equals("Candidate")) {
                try {
                    dto.setReceiverTitle((String) getPropertyValue(receiver, "title"));
                    dto.setReceiverLocation((String) getPropertyValue(receiver, "location"));
                    dto.setReceiverPhoto((String) getPropertyValue(receiver, "photo"));
                } catch (Exception e) {
                    // Log the error but continue with partial data
                    System.err.println("Error getting Candidate properties: " + e.getMessage());
                }
            } else if (className.equals("Company")) {
                try {
                    dto.setReceiverCompany((String) getPropertyValue(receiver, "name"));
                } catch (Exception e) {
                    // Log the error but continue with partial data
                    System.err.println("Error getting Company properties: " + e.getMessage());
                }
            }
        }
        
        dto.setStatus(connection.getStatus().name());
        dto.setCreatedAt(connection.getCreatedAt());
        dto.setUpdatedAt(connection.getUpdatedAt());
        
        // Set whether the current user is the requester
        dto.setUserRequester(currentUserId != null && 
            currentUserId.equals(connection.getRequester().getId()));
        
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
            System.err.println("Error accessing property " + propertyName + ": " + e.getMessage());
            return null;
        }
    }
} 