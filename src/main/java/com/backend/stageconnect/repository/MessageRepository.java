package com.backend.stageconnect.repository;

import com.backend.stageconnect.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // Find messages by conversation ID (ordered by timestamp)
    List<Message> findByConversationIdOrderByCreatedAtAsc(String conversationId);
    
    // Find messages between two users (ordered by timestamp)
    @Query("SELECT m FROM Message m WHERE " +
           "((m.sender.id = :userId1 AND m.receiver.id = :userId2) OR " +
           "(m.sender.id = :userId2 AND m.receiver.id = :userId1)) " +
           "ORDER BY m.createdAt ASC")
    List<Message> findMessagesBetweenUsers(
        @Param("userId1") Long userId1, 
        @Param("userId2") Long userId2
    );
    
    // Find paginated messages between two users (ordered by timestamp)
    @Query("SELECT m FROM Message m WHERE " +
           "((m.sender.id = :userId1 AND m.receiver.id = :userId2) OR " +
           "(m.sender.id = :userId2 AND m.receiver.id = :userId1)) " +
           "ORDER BY m.createdAt DESC")
    Page<Message> findMessagesBetweenUsers(
        @Param("userId1") Long userId1, 
        @Param("userId2") Long userId2,
        Pageable pageable
    );
    
    // Find unread messages for a user
    List<Message> findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(Long receiverId);
    
    // Count unread messages for a user
    long countByReceiverIdAndIsReadFalse(Long receiverId);
    
    // Count unread messages from a specific sender
    long countByReceiverIdAndSenderIdAndIsReadFalse(Long receiverId, Long senderId);
    
    // Find the latest message between two users
    @Query("SELECT m FROM Message m WHERE " +
           "((m.sender.id = :userId1 AND m.receiver.id = :userId2) OR " +
           "(m.sender.id = :userId2 AND m.receiver.id = :userId1)) " +
           "ORDER BY m.createdAt DESC")
    List<Message> findLatestMessageBetweenUsers(
        @Param("userId1") Long userId1, 
        @Param("userId2") Long userId2, 
        Pageable pageable
    );
    
    // Find all users that the given user has messaged with (conversations)
    @Query("SELECT DISTINCT " +
           "CASE WHEN m.sender.id = :userId THEN m.receiver.id ELSE m.sender.id END " +
           "FROM Message m " +
           "WHERE m.sender.id = :userId OR m.receiver.id = :userId")
    List<Long> findUserConversationPartnerIds(@Param("userId") Long userId);
    
    // Get the latest message for each conversation a user is involved in
    @Query(value = "SELECT m.* FROM messages m " +
                  "JOIN (SELECT CASE " +
                  "         WHEN sender_id = :userId THEN receiver_id " +
                  "         ELSE sender_id " +
                  "       END AS partner_id, " +
                  "       MAX(created_at) as max_date " +
                  "FROM messages " +
                  "WHERE sender_id = :userId OR receiver_id = :userId " +
                  "GROUP BY partner_id) latest " +
                  "ON ((m.sender_id = :userId AND m.receiver_id = latest.partner_id) OR " +
                  "    (m.receiver_id = :userId AND m.sender_id = latest.partner_id)) " +
                  "AND m.created_at = latest.max_date " +
                  "ORDER BY m.created_at DESC", 
           nativeQuery = true)
    List<Message> findLatestMessagesForUser(@Param("userId") Long userId);
} 