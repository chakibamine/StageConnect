package com.backend.stageconnect.dto;

import com.backend.stageconnect.entity.Comment;
import com.backend.stageconnect.entity.Post;
import com.backend.stageconnect.entity.PostLike;
import com.backend.stageconnect.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class EnhancedPostDTO extends PostDTO {
    private List<CommentDTO> comments;
    private List<UserDTO> likedBy;
    
    public static EnhancedPostDTO fromEntity(
            Post post, 
            List<Comment> comments, 
            List<PostLike> likes,
            List<Long> likedPostIds) {
        
        EnhancedPostDTO dto = new EnhancedPostDTO();
        
        // Set basic post fields from parent
        dto.setId(post.getId());
        dto.setContent(post.getContent());
        dto.setImageUrl(post.getImageUrl());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setLikeCount(post.getLikeCount());
        dto.setCommentCount(post.getCommentCount());
        
        // Set author info
        if (post.getAuthor() != null) {
            dto.setAuthorId(post.getAuthor().getId());
            dto.setAuthorName(post.getAuthor().getFirstName() + " " + post.getAuthor().getLastName());
            
            // Handle different user types
            if (post.getAuthor() instanceof com.backend.stageconnect.entity.Candidate) {
                com.backend.stageconnect.entity.Candidate candidate = (com.backend.stageconnect.entity.Candidate) post.getAuthor();
                dto.setAuthorProfileImage(candidate.getPhoto());
            } else if (post.getAuthor() instanceof com.backend.stageconnect.entity.Responsible) {
                // For company representatives, we might want to use company logo
                com.backend.stageconnect.entity.Responsible responsible = (com.backend.stageconnect.entity.Responsible) post.getAuthor();
                if (responsible.getCompany() != null) {
                    dto.setAuthorProfileImage(responsible.getCompany().getPhoto());
                }
            }
        }
        
        // Set whether current user has liked this post
        dto.setLikedByCurrentUser(likedPostIds != null && likedPostIds.contains(post.getId()));
        
        // Transform all comments
        if (comments != null && !comments.isEmpty()) {
            // Filter to only top-level comments (no parent)
            List<Comment> topLevelComments = comments.stream()
                    .filter(comment -> comment.getParentComment() == null)
                    .collect(Collectors.toList());
            
            dto.setComments(topLevelComments.stream()
                    .map(comment -> CommentDTO.fromEntity(comment, true))
                    .collect(Collectors.toList()));
        } else {
            dto.setComments(Collections.emptyList());
        }
        
        // Transform users who liked the post
        if (likes != null && !likes.isEmpty()) {
            dto.setLikedBy(likes.stream()
                    .map(like -> {
                        User user = like.getUser();
                        return new UserDTO(
                                user.getId(),
                                user.getFirstName(),
                                user.getLastName(),
                                getUserProfileImage(user)
                        );
                    })
                    .collect(Collectors.toList()));
        } else {
            dto.setLikedBy(Collections.emptyList());
        }
        
        return dto;
    }
    
    private static String getUserProfileImage(User user) {
        if (user instanceof com.backend.stageconnect.entity.Candidate) {
            return ((com.backend.stageconnect.entity.Candidate) user).getPhoto();
        } else if (user instanceof com.backend.stageconnect.entity.Responsible) {
            com.backend.stageconnect.entity.Responsible responsible = 
                (com.backend.stageconnect.entity.Responsible) user;
            if (responsible.getCompany() != null) {
                return responsible.getCompany().getPhoto();
            }
        }
        return null;
    }
} 