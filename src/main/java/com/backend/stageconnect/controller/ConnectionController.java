package com.backend.stageconnect.controller;

import com.backend.stageconnect.dto.ConnectionDTO;
import com.backend.stageconnect.entity.Connection;
import com.backend.stageconnect.entity.User;
import com.backend.stageconnect.repository.ConnectionRepository;
import com.backend.stageconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/connections")
@CrossOrigin(
    origins = {"http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173", "http://127.0.0.1:3000"},
    allowCredentials = "true",
    allowedHeaders = {"Origin", "Content-Type", "Accept", "Authorization", "X-Requested-With", "Access-Control-Request-Method", "Access-Control-Request-Headers", "x-cors-debug"},
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
public class ConnectionController {

    @Autowired
    private ConnectionRepository connectionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Get all connections for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserConnections(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            // Check if the user exists
            if (!userRepository.existsById(userId)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found with ID: " + userId
                ));
            }
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Connection> connectionsPage = connectionRepository.findConnectionsByUserId(userId, pageable);
            
            List<ConnectionDTO> connections = connectionsPage.getContent().stream()
                    .map(connection -> ConnectionDTO.fromEntity(connection, userId))
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("connections", connections);
            response.put("currentPage", connectionsPage.getNumber());
            response.put("totalItems", connectionsPage.getTotalElements());
            response.put("totalPages", connectionsPage.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to fetch connections: " + e.getMessage()
            ));
        }
    }
    
    // Get pending connection requests for a user
    @GetMapping("/pending/{userId}")
    public ResponseEntity<?> getPendingConnections(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            // Check if the user exists
            if (!userRepository.existsById(userId)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found with ID: " + userId
                ));
            }
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Connection> pendingPage = connectionRepository.findByReceiverIdAndStatus(
                userId, Connection.ConnectionStatus.PENDING, pageable);
            
            List<ConnectionDTO> pendingConnections = pendingPage.getContent().stream()
                    .map(connection -> ConnectionDTO.fromEntity(connection, userId))
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("pendingConnections", pendingConnections);
            response.put("currentPage", pendingPage.getNumber());
            response.put("totalItems", pendingPage.getTotalElements());
            response.put("totalPages", pendingPage.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to fetch pending connections: " + e.getMessage()
            ));
        }
    }
    
    // Send a connection request
    @PostMapping("/request/{receiverId}")
    public ResponseEntity<?> sendConnectionRequest(
            @PathVariable Long receiverId,
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            // Extract user ID from request body
            Long requesterId = Long.valueOf(requestBody.get("user_id").toString());
            
            // Check if users exist
            User requester = userRepository.findById(requesterId).orElse(null);
            User receiver = userRepository.findById(receiverId).orElse(null);
            
            if (requester == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Requester not found with ID: " + requesterId
                ));
            }
            
            if (receiver == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Receiver not found with ID: " + receiverId
                ));
            }
            
            // Check if users are the same
            if (requesterId.equals(receiverId)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "You cannot send a connection request to yourself"
                ));
            }
            
            // Check if a connection already exists
            Optional<Connection> existingConnection = connectionRepository.findExistingConnection(
                requesterId, receiverId);
            
            if (existingConnection.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "A connection already exists between these users"
                ));
            }
            
            // Check if a pending request already exists
            Optional<Connection> pendingRequest = connectionRepository.findByRequesterIdAndReceiverId(
                requesterId, receiverId);
            
            if (pendingRequest.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "A connection request is already pending"
                ));
            }
            
            // Create new connection request
            Connection connection = new Connection();
            connection.setRequester(requester);
            connection.setReceiver(receiver);
            connection.setStatus(Connection.ConnectionStatus.PENDING);
            
            Connection savedConnection = connectionRepository.save(connection);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Connection request sent successfully",
                "data", ConnectionDTO.fromEntity(savedConnection, requesterId)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to send connection request: " + e.getMessage()
            ));
        }
    }
    
    // Accept a connection request
    @PutMapping("/{connectionId}/accept")
    public ResponseEntity<?> acceptConnectionRequest(
            @PathVariable Long connectionId,
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            // Extract user ID from request body
            Long userId = Long.valueOf(requestBody.get("user_id").toString());
            
            return connectionRepository.findById(connectionId)
                    .map(connection -> {
                        // Check if the current user is the receiver of the request
                        if (!connection.getReceiver().getId().equals(userId)) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                                "success", false,
                                "message", "You are not authorized to accept this connection request"
                            ));
                        }
                        
                        // Check if the connection is pending
                        if (connection.getStatus() != Connection.ConnectionStatus.PENDING) {
                            return ResponseEntity.badRequest().body(Map.of(
                                "success", false,
                                "message", "This connection request is not pending"
                            ));
                        }
                        
                        // Update connection status
                        connection.setStatus(Connection.ConnectionStatus.CONNECTED);
                        Connection updatedConnection = connectionRepository.save(connection);
                        
                        return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Connection request accepted",
                            "data", ConnectionDTO.fromEntity(updatedConnection, userId)
                        ));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to accept connection request: " + e.getMessage()
            ));
        }
    }
    
    // Reject a connection request
    @PutMapping("/{connectionId}/reject")
    public ResponseEntity<?> rejectConnectionRequest(
            @PathVariable Long connectionId,
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            // Extract user ID from request body
            Long userId = Long.valueOf(requestBody.get("user_id").toString());
            
            return connectionRepository.findById(connectionId)
                    .map(connection -> {
                        // Check if the current user is the receiver of the request
                        if (!connection.getReceiver().getId().equals(userId)) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                                "success", false,
                                "message", "You are not authorized to reject this connection request"
                            ));
                        }
                        
                        // Check if the connection is pending
                        if (connection.getStatus() != Connection.ConnectionStatus.PENDING) {
                            return ResponseEntity.badRequest().body(Map.of(
                                "success", false,
                                "message", "This connection request is not pending"
                            ));
                        }
                        
                        // Update connection status
                        connection.setStatus(Connection.ConnectionStatus.REJECTED);
                        Connection updatedConnection = connectionRepository.save(connection);
                        
                        return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Connection request rejected",
                            "data", ConnectionDTO.fromEntity(updatedConnection, userId)
                        ));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to reject connection request: " + e.getMessage()
            ));
        }
    }
    
    // Remove a connection (for connected users only)
    @DeleteMapping("/{connectionId}")
    public ResponseEntity<?> removeConnection(
            @PathVariable Long connectionId,
            @RequestParam Long userId) {
        
        try {
            return connectionRepository.findById(connectionId)
                    .map(connection -> {
                        // Check if the current user is part of the connection
                        boolean isUserPartOfConnection = 
                            connection.getRequester().getId().equals(userId) || 
                            connection.getReceiver().getId().equals(userId);
                        
                        if (!isUserPartOfConnection) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                                "success", false,
                                "message", "You are not authorized to remove this connection"
                            ));
                        }
                        
                        // Check if the connection is established
                        if (connection.getStatus() != Connection.ConnectionStatus.CONNECTED) {
                            return ResponseEntity.badRequest().body(Map.of(
                                "success", false,
                                "message", "This is not an established connection"
                            ));
                        }
                        
                        // Delete the connection
                        connectionRepository.delete(connection);
                        
                        return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Connection removed successfully"
                        ));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to remove connection: " + e.getMessage()
            ));
        }
    }
    
    // Get connection suggestions (this would typically involve a more complex algorithm)
    @GetMapping("/suggestions/{userId}")
    public ResponseEntity<?> getConnectionSuggestions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            // Check if the user exists
            if (!userRepository.existsById(userId)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found with ID: " + userId
                ));
            }
            
            // This is a simplified implementation for suggestions
            // A real implementation would include more complex logic like:
            // - Users with mutual connections
            // - Users in the same industry/company
            // - Users with similar skills/interests
            // - Users who viewed the same internships
            
            Pageable pageable = PageRequest.of(page, size);
            
            // Get a sample of users excluding the current user and existing connections
            // This is just a placeholder - implement a more sophisticated query based on your needs
            @SuppressWarnings("unchecked")
            List<User> suggestions = userRepository.findAll(pageable).getContent().stream()
                    .filter(user -> !user.getId().equals(userId))
                    .limit(size)
                    .collect(Collectors.toList());
            
            // Convert to DTOs (without actual connection objects)
            // In a real implementation, you'd want to add info like mutual connections count
            List<Map<String, Object>> suggestionsList = suggestions.stream()
                    .map(user -> {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("id", user.getId());
                        userMap.put("name", user.getFirstName() + " " + user.getLastName());
                        
                        // Add more user details based on your User entity structure
                        // This is just a simplified example
                        
                        return userMap;
                    })
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("suggestions", suggestionsList);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to fetch connection suggestions: " + e.getMessage()
            ));
        }
    }
    
    // Get connection statistics for a user
    @GetMapping("/stats/{userId}")
    public ResponseEntity<?> getConnectionStats(@PathVariable Long userId) {
        try {
            // Check if the user exists
            if (!userRepository.existsById(userId)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found with ID: " + userId
                ));
            }
            
            long connectionsCount = connectionRepository.countConnectionsByUserId(userId);
            long pendingCount = connectionRepository.countByReceiverIdAndStatus(
                userId, Connection.ConnectionStatus.PENDING);
            
            // Add more statistics as needed
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("connectionsCount", connectionsCount);
            stats.put("pendingCount", pendingCount);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to fetch connection statistics: " + e.getMessage()
            ));
        }
    }
} 