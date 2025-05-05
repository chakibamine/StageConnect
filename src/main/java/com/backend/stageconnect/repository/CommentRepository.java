package com.backend.stageconnect.repository;

import com.backend.stageconnect.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // Find comments for a specific post (only top-level comments)
    Page<Comment> findByPostIdAndParentCommentIsNullOrderByCreatedAtDesc(Long postId, Pageable pageable);
    
    // Find replies for a specific comment
    List<Comment> findByParentCommentIdOrderByCreatedAtAsc(Long parentId);
    
    // Count top-level comments for a post
    Long countByPostIdAndParentCommentIsNull(Long postId);
    
    // Count all comments for a post (both top-level and replies)
    Long countByPostId(Long postId);
    
    // Find comments by author
    Page<Comment> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);
    
    // Delete all comments for a post
    void deleteByPostId(Long postId);
} 