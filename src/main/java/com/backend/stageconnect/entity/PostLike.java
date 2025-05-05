package com.backend.stageconnect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "post_likes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostLike {
    
    @EmbeddedId
    private PostLikeId id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "post_id")
    private Post post;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostLikeId implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        @Column(name = "user_id")
        private Long userId;
        
        @Column(name = "post_id")
        private Long postId;
    }
} 