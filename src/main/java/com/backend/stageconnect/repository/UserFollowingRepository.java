package com.backend.stageconnect.repository;

import com.backend.stageconnect.entity.User;
import com.backend.stageconnect.entity.UserFollowing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFollowingRepository extends JpaRepository<UserFollowing, Long> {
    // Find a specific following relationship
    Optional<UserFollowing> findByFollowerAndFollowed(User follower, User followed);
    
    // Check if a user is following another user
    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);
    
    // Get all users that a specific user is following
    List<UserFollowing> findByFollowerId(Long followerId);
    
    // Get all users that are following a specific user
    List<UserFollowing> findByFollowedId(Long followedId);
    
    // Get all users that a specific user is following (paginated)
    Page<UserFollowing> findByFollowerId(Long followerId, Pageable pageable);
    
    // Get all users that are following a specific user (paginated)
    Page<UserFollowing> findByFollowedId(Long followedId, Pageable pageable);
    
    // Delete a following relationship
    void deleteByFollowerIdAndFollowedId(Long followerId, Long followedId);
    
    // Count followers
    long countByFollowedId(Long userId);
    
    // Count following
    long countByFollowerId(Long userId);
} 