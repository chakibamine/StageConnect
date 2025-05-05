package com.backend.stageconnect.dto;

import com.backend.stageconnect.entity.Post;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostDTO {
    private Long id;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
    private Integer likeCount;
    private Integer commentCount;
    private Long authorId;
    private String authorName;
    private String authorProfileImage;

    public static PostDTO fromEntity(Post post) {
        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setContent(post.getContent());
        dto.setImageUrl(post.getImageUrl());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setLikeCount(post.getLikeCount());
        dto.setCommentCount(post.getCommentCount());
        
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
        
        return dto;
    }
} 