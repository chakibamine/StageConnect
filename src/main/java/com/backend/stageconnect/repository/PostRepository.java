package com.backend.stageconnect.repository;

import com.backend.stageconnect.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // Find posts by author ID
    List<Post> findByAuthorId(Long authorId);
    
    // Find posts by author ID with pagination
    Page<Post> findByAuthorId(Long authorId, Pageable pageable);
    
    // Find posts ordered by creation date (newest first) with pagination
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // Find posts from users that a specific user follows
    @Query("SELECT p FROM Post p WHERE p.author.id IN " +
           "(SELECT f.followed.id FROM UserFollowing f WHERE f.follower.id = :userId) " +
           "OR p.author.id = :userId " +
           "ORDER BY p.createdAt DESC")
    Page<Post> findFeedPostsForUser(Long userId, Pageable pageable);
    
    // Search posts by content
    Page<Post> findByContentContainingIgnoreCase(String keyword, Pageable pageable);
} 