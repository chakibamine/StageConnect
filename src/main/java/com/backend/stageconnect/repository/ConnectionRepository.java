package com.backend.stageconnect.repository;

import com.backend.stageconnect.entity.Connection;
import com.backend.stageconnect.entity.Connection.ConnectionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectionRepository extends JpaRepository<Connection, Long> {
    
    // Find all connections (CONNECTED status) for a user (either as requester or receiver)
    @Query("SELECT c FROM Connection c WHERE (c.requester.id = :userId OR c.receiver.id = :userId) AND c.status = 'CONNECTED'")
    Page<Connection> findConnectionsByUserId(@Param("userId") Long userId, Pageable pageable);
    
    // Find all pending connection requests sent to a user
    Page<Connection> findByReceiverIdAndStatus(Long receiverId, ConnectionStatus status, Pageable pageable);
    
    // Find all pending connection requests sent by a user
    Page<Connection> findByRequesterIdAndStatus(Long requesterId, ConnectionStatus status, Pageable pageable);
    
    // Check if a connection exists between two users (in either direction)
    @Query("SELECT c FROM Connection c WHERE " +
           "((c.requester.id = :user1Id AND c.receiver.id = :user2Id) OR " +
           "(c.requester.id = :user2Id AND c.receiver.id = :user1Id)) AND " +
           "c.status = 'CONNECTED'")
    Optional<Connection> findExistingConnection(
        @Param("user1Id") Long user1Id, 
        @Param("user2Id") Long user2Id
    );
    
    // Find a connection request between two specific users
    Optional<Connection> findByRequesterIdAndReceiverId(Long requesterId, Long receiverId);
    
    // Count a user's connections
    @Query("SELECT COUNT(c) FROM Connection c WHERE " +
           "(c.requester.id = :userId OR c.receiver.id = :userId) AND " +
           "c.status = 'CONNECTED'")
    long countConnectionsByUserId(@Param("userId") Long userId);
    
    // Count pending connection requests for a user
    long countByReceiverIdAndStatus(Long receiverId, ConnectionStatus status);
} 