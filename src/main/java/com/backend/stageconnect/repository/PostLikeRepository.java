package com.backend.stageconnect.repository;

import com.backend.stageconnect.entity.PostLike;
import com.backend.stageconnect.entity.PostLike.PostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {
    
    // Check if a user has liked a post
    boolean existsByIdUserIdAndIdPostId(Long userId, Long postId);
    
    // Count likes for a post
    long countByIdPostId(Long postId);
    
    // Find all post likes for a user
    List<PostLike> findByIdUserId(Long userId);
    
    // Find all post likes for a post
    List<PostLike> findByIdPostId(Long postId);
    
    // Delete a like by user and post
    void deleteByIdUserIdAndIdPostId(Long userId, Long postId);
    
    // Get all post IDs liked by a user
    @Query("SELECT pl.id.postId FROM PostLike pl WHERE pl.id.userId = ?1")
    List<Long> findPostIdsByUserId(Long userId);
} 