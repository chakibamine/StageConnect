package com.backend.stageconnect.dto;

import com.backend.stageconnect.entity.Candidate;
import com.backend.stageconnect.entity.Comment;
import com.backend.stageconnect.entity.Responsible;
import com.backend.stageconnect.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private Long id;
    private String content;
    private Long authorId;
    private String authorName;
    private String authorProfilePic;
    private Long postId;
    private Long parentId;
    private int likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentDTO> replies;

    // Convert from entity to DTO
    public static CommentDTO fromEntity(Comment comment, boolean includeReplies) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        
        User author = comment.getAuthor();
        dto.setAuthorId(author.getId());
        dto.setAuthorName(author.getFirstName() + " " + author.getLastName());
        
        // Handle profile picture based on user type
        if (author instanceof Candidate) {
            dto.setAuthorProfilePic(((Candidate) author).getPhoto());
        } else if (author instanceof Responsible) {
            // Try to get company photo if available
            try {
                dto.setAuthorProfilePic(((Responsible) author).getCompany().getPhoto());
            } catch (Exception e) {
                dto.setAuthorProfilePic(null); // Set to null if not available
            }
        } else {
            dto.setAuthorProfilePic(null);
        }
        
        if (comment.getPost() != null) {
            dto.setPostId(comment.getPost().getId());
        }
        
        if (comment.getParentComment() != null) {
            dto.setParentId(comment.getParentComment().getId());
        }
        
        dto.setLikeCount(comment.getLikeCount());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        
        if (includeReplies && comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            dto.setReplies(comment.getReplies().stream()
                    .map(reply -> CommentDTO.fromEntity(reply, false)) // Avoid deep nesting
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
} 